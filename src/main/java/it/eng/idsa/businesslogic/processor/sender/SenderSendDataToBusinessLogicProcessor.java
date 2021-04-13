package it.eng.idsa.businesslogic.processor.sender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.sender.websocket.client.MessageWebSocketOverHttpSender;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@Component
public class SenderSendDataToBusinessLogicProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderSendDataToBusinessLogicProcessor.class);
	
	public static final String REGEX_WSS = "(wss://)([^:^/]*)(:)(\\d*)";

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Value("${camel.component.jetty.use-global-ssl-context-parameters}")
	private boolean isJettySSLEnabled;

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Autowired
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;

	@Autowired
	private MessageWebSocketOverHttpSender messageWebSocketOverHttpSender;

	private String webSocketHost;
	private Integer webSocketPort;

	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		Map<String, Object> headerParts = exchange.getMessage().getHeaders();

		String payload = null;
		Message message = null;
		String header =null;

		payload = multipartMessage.getPayloadContent();
		header= multipartMessage.getHeaderContentString();
		message = multipartMessage.getHeaderContent();

		String forwardTo = headerParts.get("Forward-To").toString();

		if (isEnabledWebSocket) {
			// check & exstract HTTPS WebSocket IP and Port
			try {
				this.extractWebSocketIPAndPort(forwardTo, REGEX_WSS);
			} catch (Exception e) {
				logger.info("... bad wss URL - '{}', {}", forwardTo, e.getMessage());
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
						message);
			}

			// -- Send data using HTTPS - (Client) - WebSocket
			String response = messageWebSocketOverHttpSender.sendMultipartMessageWebSocketOverHttps(this.webSocketHost,
						this.webSocketPort, header, payload, message);
			// Handle response
			this.handleResponseWebSocket(exchange, message, response, forwardTo);
		} else {
			// Send MultipartMessage HTTPS
			Response response = this.sendMultipartMessage(headerParts,	forwardTo, message,  multipartMessage);
			// Check response
			sendDataToBusinessLogicService.checkResponse(message, response, forwardTo);
			// Handle response
			this.handleResponse(exchange, message, response, forwardTo);

			if (response != null) {
				response.close();
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
				response = sendDataToBusinessLogicService.sendMessageBinary(forwardTo, multipartMessage, headerParts, true);
				break;
			}
			case "form": {
				response = sendDataToBusinessLogicService.sendMessageFormData(forwardTo, multipartMessage, headerParts, true);
				break;
			}
			case "http-header": {
				response = sendDataToBusinessLogicService.sendMessageHttpHeader(forwardTo, multipartMessage, headerParts, true);
				break;
			}
			default:
				logger.error("Applicaton property: application.eccHttpSendRouter is not properly set");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES,
						message);
			}
		return response;
	}


	private void handleResponse(Exchange exchange, Message message, Response response, String forwardTo)
			throws UnsupportedOperationException, IOException {
		String responseString = getResponseBodyAsString(response);
		logger.info("data received from destination " + forwardTo);
		logger.info("response received from the DataAPP=" + responseString);

		exchange.getMessage().setHeaders(returnHeadersAsMap(response.headers()));
		if (RouterType.HTTP_HEADER.label.equals(eccHttpSendRouter)) {
			exchange.getMessage().setBody(responseString);
		} else {
			exchange.getMessage().setHeader("header", multipartMessageService.getHeaderContentString(responseString));
			exchange.getMessage().setHeader("payload", multipartMessageService.getPayloadContent(responseString));

		}
	}

	private void handleResponseWebSocket(Exchange exchange, Message message, String responseString, String forwardTo) {
		if (responseString == null) {
			logger.info("...communication error");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		} else {
//			logger.info("response received from the DataAPP=" + responseString);
			logger.info("data sent to destination " + forwardTo);
//			logger.info("Successful response: " + responseString);
			// TODO:
			// Set original body which is created using the original payload and header
			exchange.getMessage().setHeader("header", multipartMessageService.getHeaderContentString(responseString));
			exchange.getMessage().setHeader("payload", multipartMessageService.getPayloadContent(responseString));
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

	private Map<String, Object> returnHeadersAsMap(Headers headers) {
		Map<String, List<String>> multiMap = headers.toMultimap();
		Map<String, Object> result = 
				multiMap.entrySet()
			           .stream()
			           .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(", ", e.getValue())));
		return result;
	}
}
