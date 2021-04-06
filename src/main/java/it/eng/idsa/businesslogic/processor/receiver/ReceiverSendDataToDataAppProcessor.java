package it.eng.idsa.businesslogic.processor.receiver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.SendDataToBusinessLogicServiceImpl;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ReceiverSendDataToDataAppProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ReceiverSendDataToDataAppProcessor.class);

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Value("${application.isEnabledUsageControl:false}")
	private boolean isEnabledUsageControl;

	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	private String originalHeader;

	@Autowired
	private SendDataToBusinessLogicServiceImpl sendDataToBusinessLogicService;

	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;
	
	@Autowired
	private HttpHeaderService httpHeaderService;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headerParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		
		if (!"http-header".equals(openDataAppReceiverRouter)) {
        	httpHeaderService.removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
		}

		// Get header, payload and message
		Message message = null;

		this.originalHeader = multipartMessage.getHeaderContentString();
		// Send data to the endpoint F for the Open API Data App
		Response response = null;
		switch (openDataAppReceiverRouter) {
			case "mixed": {
				response = sendDataToBusinessLogicService.sendMessageBinary(configuration.getOpenDataAppReceiver(),
						multipartMessage, headerParts, false);
				break;
			}
			case "form": {
				response = sendDataToBusinessLogicService.sendMessageFormData(configuration.getOpenDataAppReceiver(),
						multipartMessage, headerParts, false);
				break;
			}
			case "http-header": {
				response = sendDataToBusinessLogicService.sendMessageHttpHeader(configuration.getOpenDataAppReceiver(),
						multipartMessage, headerParts, false);
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

		if (response != null) {
			response.close();
		}
	}

	private void handleResponse(Exchange exchange, Message message, Response response, String openApiDataAppAddress)
			throws UnsupportedOperationException, IOException {
		String responseString = getResponseBodyAsString(response);
		logger.info("data sent to destination: " + openApiDataAppAddress);
		logger.info("response received from the DataAPP=" + responseString);

		exchange.getMessage().setHeaders(returnHeadersAsMap(response.headers()));
		if ("http-header".equals(openDataAppReceiverRouter)) {
			exchange.getMessage().setBody(responseString);
		} else {
			exchange.getMessage().setHeader("header", multipartMessageService.getHeaderContentString(responseString));
			exchange.getMessage().setHeader("payload", multipartMessageService.getPayloadContent(responseString));
		}

		if (isEnabledUsageControl) {
			exchange.getMessage().setHeader("Original-Message-Header", originalHeader);
		}
	}
	
	private String getResponseBodyAsString(Response response) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (int length; (length = response.body().byteStream().read(buffer)) != -1;) {
			outputStream.write(buffer, 0, length);
		}
		String responseString = outputStream.toString("UTF-8");
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
