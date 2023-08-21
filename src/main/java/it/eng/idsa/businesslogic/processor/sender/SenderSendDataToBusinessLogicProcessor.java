package it.eng.idsa.businesslogic.processor.sender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.processor.sender.websocket.client.MessageWebSocketOverHttpSender;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.util.Helper;
import it.eng.idsa.businesslogic.util.OCSPValidation;
import it.eng.idsa.businesslogic.util.OCSPValidation.OCSP_STATUS;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.MultipartMessageKey;
import okhttp3.Response;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@Component
public class SenderSendDataToBusinessLogicProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(SenderSendDataToBusinessLogicProcessor.class);

	public static final String REGEX_WSS = "(wss://)([^:^/]*)(:)(\\d*)";

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Value("${camel.component.jetty.use-global-ssl-context-parameters}")
	private boolean isJettySSLEnabled;

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Value("${application.OCSP_RevocationCheckValue:none}")
	private OCSP_STATUS desideredOCSPRevocationCheckValue;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Autowired
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;

	@Autowired
	private MessageWebSocketOverHttpSender messageWebSocketOverHttpSender;

	@Autowired
	private HttpHeaderService httpHeaderService;

	private String webSocketHost;
	private Integer webSocketPort;

	@Override
	@CamelAuditable(beforeEventType =  TrueConnectorEventType.CONNECTOR_SEND,
	successEventType = TrueConnectorEventType.CONNECTOR_RESPONSE, 
	failureEventType = TrueConnectorEventType.EXCEPTION_SERVER_ERROR)
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		Map<String, Object> headerParts = exchange.getMessage().getHeaders();

		String payload = null;
		Message message = null;
		String header =null;

		payload = multipartMessage.getPayloadContent();
		header= multipartMessage.getHeaderContentString();

		String forwardTo = (String) headerParts.get("Forward-To");

		boolean ocspCheck = false;
		if(OCSP_STATUS.none.equals(desideredOCSPRevocationCheckValue)) {
			logger.info("Skipping OCSP validation");
			ocspCheck = true;
		} else {
			ocspCheck = OCSPValidation.checkOCSPCerificate(forwardTo, desideredOCSPRevocationCheckValue);
		}
		
		if(!ocspCheck) {
			rejectionMessageService.sendRejectionMessage(message, RejectionReason.NOT_AUTHENTICATED);
		}
		
		logger.info("Sending data to business logic ...");
		if (isEnabledWebSocket) {
			// check & extract HTTPS WebSocket IP and Port
			try {
				this.extractWebSocketIPAndPort(forwardTo, REGEX_WSS);
			} catch (Exception e) {
				logger.info("... bad wss URL - '{}', {}", forwardTo, e.getMessage());
				rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.BAD_PARAMETERS);
			}

			// -- Send data using HTTPS - (Client) - WebSocket
			String response = messageWebSocketOverHttpSender.sendMultipartMessageWebSocketOverHttps(this.webSocketHost,
					this.webSocketPort, header, payload, (Message) exchange.getProperty("Original-Message-Header"));
			// Handle response
			this.handleResponseWebSocket(exchange, message, response, forwardTo);
		} else {
			Response httpResponse = null;
			try {
				// Send MultipartMessage HTTPS
				httpResponse = this.sendMultipartMessage(headerParts, forwardTo, message, multipartMessage);
				// Check response
				sendDataToBusinessLogicService.checkResponse((Message) exchange.getProperty("Original-Message-Header"), httpResponse, forwardTo);
				// Handle response
				this.handleResponse(exchange, message, httpResponse, forwardTo);
			} finally {
				if (httpResponse != null) {
					httpResponse.close();
				}
			}
		}
	}

	private Response sendMultipartMessage(Map<String, Object> headerParts, String forwardTo, Message message, MultipartMessage multipartMessage)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException,
			ExecutionException, UnsupportedEncodingException {
		Response response = null;
		// -- Send message using HTTPS
		switch (eccHttpSendRouter) {
		case "mixed": {
			response = sendDataToBusinessLogicService.sendMessageBinary(forwardTo, multipartMessage, headerParts);
			break;
		}
		case "form": {
			response = sendDataToBusinessLogicService.sendMessageFormData(forwardTo, multipartMessage, headerParts);
			break;
		}
		case "http-header": {
			response = sendDataToBusinessLogicService.sendMessageHttpHeader(forwardTo, multipartMessage, headerParts);
			break;
		}
		default:
			logger.error("Applicaton property: application.eccHttpSendRouter is not properly set");
			rejectionMessageService.sendRejectionMessage(message, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		}
		return response;
	}


	private void handleResponse(Exchange exchange, Message message, Response response, String forwardTo)
			throws UnsupportedOperationException, IOException {
		String responseString = getResponseBodyAsString(response);
		logger.info("data received from " + forwardTo);

		exchange.getMessage().setHeaders(httpHeaderService.okHttpHeadersToMap(response.headers()));

		MultipartMessage multipartMessage = null;
		if (RouterType.HTTP_HEADER.equals(eccHttpSendRouter)) {
			message = httpHeaderService.headersToMessage(httpHeaderService.okHttpHeadersToMap(response.headers()));
			Map<String, String> headerHeaderContentType = new HashMap<>();
			headerHeaderContentType.put("Content-Type", "application/ld+json");
			Map<String, String> payloadHeaderContentType = new HashMap<>();
			payloadHeaderContentType.put("Content-Type", response.headers().get(MultipartMessageKey.CONTENT_TYPE.label));
			multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(message)
					.withHeaderHeader(headerHeaderContentType)
					.withPayloadContent(responseString)
					.withPayloadHeader(payloadHeaderContentType)
					.build();
			exchange.getMessage().setBody(multipartMessage);
		} else {
			multipartMessage = MultipartMessageProcessor.parseMultipartMessage(responseString);
			exchange.getMessage().setBody(multipartMessage);
		}
		logger.info("message received from Provider Connector : {}", Helper.getIDSMessageType(multipartMessage.getHeaderContent()));
	}

	private void handleResponseWebSocket(Exchange exchange, Message message, String responseString, String forwardTo) {
		if (responseString == null) {
			logger.info("...communication error");
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), 
					RejectionReason.INTERNAL_RECIPIENT_ERROR);
		} else {
			logger.info("data sent to " + forwardTo);
			// Set original body which is created using the original payload and header
			MultipartMessage mm = MultipartMessageProcessor.parseMultipartMessage(responseString);
			exchange.getMessage().setBody(mm);
		}
	}

	private void extractWebSocketIPAndPort(String forwardTo, String regexForwardTo) {
		// Split URL into protocol, host, port
		Pattern pattern = Pattern.compile(regexForwardTo);
		Matcher matcher = pattern.matcher(forwardTo);
		matcher.find();

		this.webSocketHost = matcher.group(2);
		this.webSocketPort = Integer.parseInt(matcher.group(4));
	}

	private String getResponseBodyAsString(Response response) {
		// We used ByteArrayOutputStream instead of IOUtils.toString because it's faster.
		// https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
		byte[] buffer = new byte[1024];
		String responseString = null;
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
			for (int length; (length = response.body().byteStream().read(buffer)) != -1;) {
				outputStream.write(buffer, 0, length);
			}
			responseString = outputStream.toString("UTF-8");
		} catch (IOException e) {
			logger.info("Error while parsing body, {}", e.getMessage());
		}
		return responseString;
	}
}
