package it.eng.idsa.businesslogic.processor.sender;

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
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.ws.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fhg.aisec.ids.comm.client.IdscpClient;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;
import it.eng.idsa.businesslogic.processor.sender.websocket.client.FileStreamingBean;
import it.eng.idsa.businesslogic.processor.sender.websocket.client.IdscpClientBean;
import it.eng.idsa.businesslogic.processor.sender.websocket.client.MessageWebSocketOverHttpSender;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@Component
public class SenderSendDataToBusinessLogicProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderSendDataToBusinessLogicProcessor.class);
	// example for the webSocketURL: idscp://localhost:8099
	public static final String REGEX_IDSCP = "(idscp://)([^:^/]*)(:)(\\d*)";
	public static final String REGEX_WSS = "(wss://)([^:^/]*)(:)(\\d*)";

	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;

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
	private WebSocketClientConfiguration webSocketClientConfiguration;

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

		if (isEnabledIdscp) {
			// check & extract IDSCP WebSocket IP and Port
			try {
				this.extractWebSocketIPAndPort(forwardTo, REGEX_IDSCP);
			} catch (Exception e) {
				logger.info("... bad idscp URL - '{}' {}", forwardTo, e.getMessage());
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
						message);
			}
			// -- Send data using IDSCP - (Client) - WebSocket
			String response = this.sendMultipartMessageWebSocket(this.webSocketHost, this.webSocketPort, header, payload,
						message);
			// Handle response
			this.handleResponseWebSocket(exchange, message, response, forwardTo);
		} else if (isEnabledWebSocket) {
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


	private void handleResponse(Exchange exchange, Message message, Response response, String forwardTo) throws UnsupportedOperationException, IOException {
		if (response == null) {
			logger.info("...communication error");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		} else {
			String responseString = response.body().string();
			logger.info("response received from the DataAPP=" + responseString);

			int statusCode = response.code();
			logger.info("status code of the response message is: " + statusCode);
			if (statusCode >= 300) {
				if (statusCode == 404) {
					logger.info("...communication error - bad forwardTo URL " + forwardTo);
					rejectionMessageService
							.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, message);
				}
				logger.info("data sent unuccessfully to destination " + forwardTo);
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			} else {
				logger.info("data received from destination " + forwardTo);
//				logger.info("Successful response: " + responseString);
				
				//TODO make the MultipartMessage here or in the ProducerParseReceivedResponseMessage
				exchange.getMessage().setHeaders(returnHeadersAsMap(response.headers()));
				if ("http-header".equals(eccHttpSendRouter)) {
					exchange.getMessage().setBody(responseString);
				}else {
					exchange.getMessage().setHeader("header", multipartMessageService.getHeaderContentString(responseString));
					exchange.getMessage().setHeader("payload", multipartMessageService.getPayloadContent(responseString));

				}
			}
		}
	}

	private Map<String, Object> returnHeadersAsMap(Headers headers) {
		Map<String, List<String>> multiMap = headers.toMultimap();
		Map<String, Object> result = 
				multiMap.entrySet()
			           .stream()
			           .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(", ", e.getValue())));
		return result;
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

	private String sendMultipartMessageWebSocket(String webSocketHost, Integer webSocketPort, String header,
			String payload, Message message) throws Exception, ParseException, IOException, KeyManagementException,
			NoSuchAlgorithmException, InterruptedException, ExecutionException {
		// Create idscpClient
		IdscpClientBean idscpClientBean = webSocketClientConfiguration.idscpClientServiceWebSocket();
		this.initializeIdscpClient(message, idscpClientBean);
		IdscpClient idscpClient = idscpClientBean.getClient();

		MultipartMessage multipartMessage = new MultipartMessageBuilder().withHeaderContent(header)
				.withPayloadContent(payload).build();
		String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage);

		// Send multipartMessage as a frames
		FileStreamingBean fileStreamingBean = webSocketClientConfiguration.fileStreamingWebSocket();
		WebSocket wsClient = this.createWebSocketConnection(idscpClient, webSocketHost, webSocketPort, message);
		// Try to connect to the Server. Wait until you are not connected to the server.
		wsClient.addWebSocketListener(webSocketClientConfiguration.inputStreamSocketListenerWebSocketClient());
		fileStreamingBean.setup(wsClient);
		fileStreamingBean.sendMultipartMessage(multipartMessageString);
		// We don't have status of the response (is it 200 OK or not). We have only the
		// content of the response.
		String responseMessage = new String(
				webSocketClientConfiguration.responseMessageBufferWebSocketClient().remove());
		this.closeWSClient(wsClient);
		logger.info("received response");
		logger.debug("content: " + responseMessage);

		return responseMessage;
	}

	private void initializeIdscpClient(Message message, IdscpClientBean idscpClientBean) {
		try {
			idscpClientBean.createIdscpClient();
		} catch (Exception e) {
			logger.info("... can not initilize the IdscpClient");
			logger.error(e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		}
	}

	private WebSocket createWebSocketConnection(IdscpClient idscpClient, String webSocketHost, Integer webSocketPort,
			Message message) {
		WebSocket wsClient = null;
		try {
			wsClient = idscpClient.connect(webSocketHost, webSocketPort);
		} catch (Exception e) {
			logger.info("... can not create the WebSocket connection IDSCP");
			logger.error(e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		}
		return wsClient;
	}

	private void extractWebSocketIPAndPort(String forwardTo, String regexForwardTo) {
		// Split URL into protocol, host, port
		Pattern pattern = Pattern.compile(regexForwardTo);
		Matcher matcher = pattern.matcher(forwardTo);
		matcher.find();

		this.webSocketHost = matcher.group(2);
		this.webSocketPort = Integer.parseInt(matcher.group(4));
	}

	private void closeWSClient(WebSocket wsClient) {
		// Send the close frame 200 (OK), "Shutdown"; in this method we also close the
		// wsClient.
		try {
			wsClient.sendCloseFrame(200, "Shutdown");
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
