package it.eng.idsa.businesslogic.processor.receiver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.SendDataToBusinessLogicServiceImpl;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.MultipartMessageKey;
import okhttp3.Response;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ReceiverSendDataToDataAppProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ReceiverSendDataToDataAppProcessor.class);

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Value("#{new Boolean('${application.isEnabledUsageControl}')}")
	private boolean isEnabledUsageControl;

	@Autowired
	private ApplicationConfiguration configuration;

//	@Autowired
//	private MultipartMessageService multipartMessageService;

	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private HttpHeaderService httpHeaderService;

	private String originalHeader;

	@Autowired
	private SendDataToBusinessLogicServiceImpl sendDataToBusinessLogicService;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;
	
//	@Autowired
//	private HttpHeaderService httpHeaderService;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headerParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		
//		if (!RouterType.HTTP_HEADER.equals(openDataAppReceiverRouter)) {
//        	httpHeaderService.removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
//		}

		// Get header, payload and message
		Message message = multipartMessage.getHeaderContent();

		this.originalHeader = multipartMessage.getHeaderContentString();
		// Send data to the endpoint F for the Open API Data App
		Response response = null;
		try {
			switch (openDataAppReceiverRouter) {
			case "mixed": {
				response = sendDataToBusinessLogicService.sendMessageBinary(configuration.getOpenDataAppReceiver(),
						multipartMessage, headerParts);
				break;
			}
			case "form": {
				response = sendDataToBusinessLogicService.sendMessageFormData(configuration.getOpenDataAppReceiver(),
						multipartMessage, headerParts);
				break;
			}
			case "http-header": {
				response = sendDataToBusinessLogicService.sendMessageHttpHeader(configuration.getOpenDataAppReceiver(),
						multipartMessage, headerParts);
				break;
			}
			default: {
				logger.error("Applicaton property: application.openDataAppReceiverRouter is not properly set");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, message);
			}
			}
			
			// Check response
			sendDataToBusinessLogicService.checkResponse(message, response, configuration.getOpenDataAppReceiver());
			// Handle response
			handleResponse(exchange, message, response, configuration.getOpenDataAppReceiver());
			
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	private void handleResponse(Exchange exchange, Message message, Response response, String openApiDataAppAddress)
			throws UnsupportedOperationException, IOException {
		String responseString = getResponseBodyAsString(response);
		logger.info("data sent to destination: " + openApiDataAppAddress);
		logger.info("response received from the DataAPP=" + responseString);

		Map<String, Object> headers = httpHeaderService.okHttpHeadersToMap(response.headers());
		exchange.getMessage().setHeaders(headers);
		if (isEnabledUsageControl) {
			exchange.getMessage().setHeader("Original-Message-Header", originalHeader);
		}
		if (RouterType.HTTP_HEADER.equals(openDataAppReceiverRouter)) {
			exchange.getMessage().setBody(responseString);
			message = httpHeaderService.headersToMessage(headers);
			
			Map<String, String> headerHeaderContentType = new HashMap<>();
			headerHeaderContentType.put("Content-Type", "application/ld+json");
			Map<String, String> payloadHeaderContentType = new HashMap<>();
			payloadHeaderContentType.put("Content-Type", response.headers().get(MultipartMessageKey.CONTENT_TYPE.label));
			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(message)
					.withHeaderHeader(headerHeaderContentType)
					.withPayloadContent(responseString)
					.withPayloadHeader(payloadHeaderContentType)
					.build();
			exchange.getMessage().setBody(multipartMessage);
			
		} else {
			MultipartMessage multipartMessage = MultipartMessageProcessor.parseMultipartMessage(responseString);
			exchange.getMessage().setHeader(MessagePart.HEADER, multipartMessage.getHeaderContentString());
			exchange.getMessage().setHeader(MessagePart.PAYLOAD, multipartMessage.getPayloadContent());
			exchange.getMessage().setBody(multipartMessage);
		}
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
