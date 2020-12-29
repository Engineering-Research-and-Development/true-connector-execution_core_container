package it.eng.idsa.businesslogic.processor.sender;

import java.util.Map;
import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
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
public class SenderSendResponseToDataAppProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(SenderSendResponseToDataAppProcessor.class);

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Autowired(required = false)
	WebSocketServerConfigurationA webSocketServerConfiguration;
	
	@Autowired
	private HttpHeaderService httpHeaderService;
	
	@Autowired
	private HeaderCleaner headerCleaner;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headerParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);

		String responseString = null;
		String contentType = null;

		if (isEnabledDapsInteraction) {
			//remove token before sending the response
			multipartMessage = multipartMessageService.removeTokenFromMultipart(multipartMessage);
		}
			switch (openDataAppReceiverRouter) {
			case "form":
				httpHeaderService.removeTokenHeaders(exchange.getMessage().getHeaders());
            	httpHeaderService.removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
				HttpEntity resultEntity = multipartMessageService.createMultipartMessage(multipartMessage.getHeaderContentString(), 
						multipartMessage.getPayloadContent(),
						null, ContentType.APPLICATION_JSON);
				contentType = resultEntity.getContentType().getValue();
				headerParts.put(Exchange.CONTENT_TYPE, contentType);
				exchange.getMessage().setBody(resultEntity.getContent());
				break;
			case "mixed":
				httpHeaderService.removeTokenHeaders(exchange.getMessage().getHeaders());
            	httpHeaderService.removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
				responseString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
				Optional<String> boundary = MultipartMessageProcessor.getMessageBoundaryFromMessage(responseString);
				contentType = "multipart/mixed; boundary=" + boundary.orElse("---aaa") + ";charset=UTF-8";
				headerParts.put(Exchange.CONTENT_TYPE, contentType);
				exchange.getMessage().setBody(responseString);
				break;
			case "http-header":
				responseString = multipartMessage.getPayloadContent();
				exchange.getMessage().setBody(responseString);
				break;
			}
			logger.info("Sending response to DataApp");

			headerCleaner.removeTechnicalHeaders(headerParts);
			exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());
		
		if(isEnabledWebSocket ) {
			String responseMultipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
			ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration.responseMessageBufferWebSocket();
			responseMessageServerBean.add(responseMultipartMessageString.getBytes());
		}
	}
	
}