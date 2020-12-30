package it.eng.idsa.businesslogic.processor.receiver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
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
		
		if (!openDataAppReceiverRouter.equals("http-header")) {
        	httpHeaderService.removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
		}

		// Get header, payload and message
		Message message = null;

		this.originalHeader = multipartMessage.getHeaderContentString();
		// Send data to the endpoint F for the Open API Data App
		CloseableHttpResponse response = null;
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

		// Handle response
		handleResponse(exchange, message, response, configuration.getOpenDataAppReceiver());

		if (response != null) {
			response.close();
		}
	}

	private void handleResponse(Exchange exchange, Message message, CloseableHttpResponse response,
			String openApiDataAppAddress) throws UnsupportedOperationException, IOException {
		if (response == null) {
			logger.info("...communication error with: " + openApiDataAppAddress);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		} else {
			String responseString = new String(response.getEntity().getContent().readAllBytes());
			logger.info("content type response received from the DataAPP=" + response.getFirstHeader("Content-Type"));
			logger.info("response received from the DataAPP=" + responseString);

			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("status code of the response message is: " + statusCode);
			if (statusCode >= 300) {
				logger.info("data sent to destination: " + openApiDataAppAddress);
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			} else {
				logger.info("data sent to destination: " + openApiDataAppAddress);
//				logger.info("Successful response from DataApp: " + responseString);

				exchange.getMessage().setHeaders(returnHeadersAsMap(response.getAllHeaders()));
				if (openDataAppReceiverRouter.equals("http-header")) {
					exchange.getMessage().setBody(responseString);
				} else {
					exchange.getMessage().setHeader("header",
							multipartMessageService.getHeaderContentString(responseString));
					exchange.getMessage().setHeader("payload", multipartMessageService.getPayloadContent(responseString));
				}

				if (isEnabledUsageControl) {
					exchange.getMessage().setHeader("Original-Message-Header", originalHeader);
				}
			}
		}
	}

	private Map<String, Object> returnHeadersAsMap(Header[] allHeaders) {
		Map<String, Object> headersMap = new HashMap<>();
		for (Header header : allHeaders) {
			headersMap.put(header.getName(), header.getValue());
		}
		return headersMap;
	}

}
