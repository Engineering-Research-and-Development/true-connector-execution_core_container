package it.eng.idsa.businesslogic.processor.receiver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.SendDataToBusinessLogicServiceImpl;
import it.eng.idsa.businesslogic.util.Helper;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.businesslogic.util.TrueConnectorConstants;
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

	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private HttpHeaderService httpHeaderService;

	@Autowired
	private SendDataToBusinessLogicServiceImpl sendDataToBusinessLogicService;
	
	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;
	
	@Override
	@CamelAuditable(beforeEventType =  TrueConnectorEventType.CONNECTOR_SEND_DATAAPP,
	successEventType = TrueConnectorEventType.CONNECTOR_RESPONSE, 
	failureEventType = TrueConnectorEventType.EXCEPTION_SERVER_ERROR)
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headerParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		
		// Get header, payload and message
		Message message = multipartMessage.getHeaderContent();

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
					rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.INTERNAL_RECIPIENT_ERROR);
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
		logger.info("data received from: {}", openApiDataAppAddress);

		Map<String, Object> headers = httpHeaderService.okHttpHeadersToMap(response.headers());
		String correlationId = (String) exchange.getMessage().getHeader(TrueConnectorConstants.CORRELATION_ID);
		if(StringUtils.isNotBlank(correlationId)) {
			headers.put(TrueConnectorConstants.CORRELATION_ID, correlationId);
		}
		exchange.getMessage().setHeaders(headers);
		MultipartMessage multipartMessage = null;
		if (RouterType.HTTP_HEADER.equals(openDataAppReceiverRouter)) {
			message = httpHeaderService.headersToMessage(headers);
			
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
		
		logger.info("Received message of type: {}", Helper.getIDSMessageType(multipartMessage.getHeaderContent()));
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
