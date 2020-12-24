package it.eng.idsa.businesslogic.processor.receiver;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ReceiverSendDataToBusinessLogicProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ReceiverSendDataToBusinessLogicProcessor.class);

	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;
	
	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired(required = false)
	private WebSocketServerConfigurationB webSocketServerConfiguration;
	
	@Autowired
	private HttpHeaderService headerService;
	
	@Autowired
	private HeaderCleaner headerCleaner;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headersParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		String responseString = null;
		String contentType = null;

		if (eccHttpSendRouter.equals("http-header")) {
			responseString = multipartMessage.getPayloadContent();
			contentType = headersParts.get("Payload-Content-Type").toString();
			headersParts.putAll(multipartMessage.getHttpHeaders());
			if(!openDataAppReceiverRouter.equals("http-header")) {
				// DataApp endpoint not http-header, must convert message to http headers
				headersParts.putAll(headerService.prepareMessageForSendingAsHttpHeaders(multipartMessage));
			}
			if (isEnabledDapsInteraction) {
				headersParts.putAll(headerService.transformJWTTokenToHeaders(multipartMessage.getToken()));
			}
		} else {
			if(isEnabledDapsInteraction) {
				responseString = MultipartMessageProcessor
						.multipartMessagetoString(multipartMessageService.addTokenToMultipartMessage(multipartMessage), false);
			} else {
				responseString = MultipartMessageProcessor
						.multipartMessagetoString(multipartMessage, false);
			}
			contentType = headersParts.getOrDefault("Content-Type", "multipart/mixed").toString();
		}

		if (isEnabledIdscp || isEnabledWebSocket) { // TODO Try to remove this config property
			ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration
					.responseMessageBufferWebSocket();
			responseMessageServerBean.add(responseString.getBytes());
		}

		headerCleaner.removeTechnicalHeaders(headersParts);
		headersParts.put("Payload-Content-Type", contentType);
		logger.info("Sending response to Data Consumer");
		
		exchange.getMessage().setBody(responseString);
		exchange.getMessage().setHeaders(headersParts);
	}
}
