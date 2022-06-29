package it.eng.idsa.businesslogic.processor.receiver;

import java.util.Map;
import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ReceiverSendDataToBusinessLogicProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(ReceiverSendDataToBusinessLogicProcessor.class);

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;
	
	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;
	
	@Autowired(required = false)
	private WebSocketServerConfigurationB webSocketServerConfiguration;
	
	@Autowired
	private HttpHeaderService headerService;
	
	@Autowired
	private HeaderCleaner headerCleaner;
	
	@Autowired
	private MultipartMessageService multipartMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headersParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		String responseString = null;

		if (RouterType.HTTP_HEADER.equals(eccHttpSendRouter)) {
			responseString = multipartMessage.getPayloadContent();
			headersParts.putAll(multipartMessage.getHttpHeaders());
			// DataApp endpoint not http-header, must convert message to http headers
			headersParts.putAll(headerService.messageToHeaders(multipartMessage.getHeaderContent()));
			exchange.getMessage().setBody(responseString);
			exchange.getMessage().setHeaders(headersParts);
//			if (isEnabledDapsInteraction) {
//				headersParts.putAll(headerService.transformJWTTokenToHeaders(multipartMessage.getToken()));
//			}
		} else {
			if (RouterType.MULTIPART_MIX.equals(eccHttpSendRouter)) {
				responseString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, Boolean.TRUE);
				Optional<String> boundary = MultipartMessageProcessor.getMessageBoundaryFromMessage(responseString);
				String contentType = "multipart/mixed; boundary=" + boundary.orElse("---aaa") + ";charset=UTF-8";
				exchange.getMessage().setBody(responseString);
				exchange.getMessage().setHeader("Content-Type", contentType);
			} else if ((RouterType.MULTIPART_BODY_FORM.equals(eccHttpSendRouter))) {
				HttpEntity resultEntity = multipartMessageService.createMultipartMessage(multipartMessage.getHeaderContentString(), 
						multipartMessage.getPayloadContent(), null, ContentType.APPLICATION_JSON);
				exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, resultEntity.getContentType().getValue());
				exchange.getMessage().setBody(resultEntity.getContent());
			}
		}

		if (isEnabledWebSocket) { // TODO Try to remove this config property
			ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration
					.responseMessageBufferWebSocket();
			responseMessageServerBean.add(responseString.getBytes());
		}

		headerCleaner.removeTechnicalHeaders(headersParts);
		logger.info("Sending response to Data Consumer");
		
//		exchange.getMessage().setBody(responseString);
//		exchange.getMessage().setHeaders(headersParts);
	}
}
