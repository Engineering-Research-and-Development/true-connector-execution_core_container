package it.eng.idsa.businesslogic.processor.sender;

import java.util.Map;
import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
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

	private static final Logger logger = LoggerFactory.getLogger(SenderSendResponseToDataAppProcessor.class);

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
		
		switch (openDataAppReceiverRouter) {
		case "form":
			//changed regarding Tecnalia problem - content lenght too long
			String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, Boolean.TRUE);
			Optional<String> boundaryy = MultipartMessageProcessor
					.getMessageBoundaryFromMessage(multipartMessageString);
			contentType = "multipart/form; boundary=" + boundaryy.orElse("---aaa") + ";charset=UTF-8";
			headerParts.put(Exchange.CONTENT_TYPE, contentType);
			exchange.getMessage().setBody(multipartMessageString);
			break;
		case "mixed":
			responseString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, Boolean.TRUE);
			
			Optional<String> boundary = MultipartMessageProcessor.getMessageBoundaryFromMessage(responseString);
			contentType = "multipart/mixed; boundary=" + boundary.orElse("---aaa") + ";charset=UTF-8";
			headerParts.put(Exchange.CONTENT_TYPE, contentType);
			
			exchange.getMessage().setBody(responseString);
			break;
		case "http-header":
			responseString = multipartMessage.getPayloadContent();
			exchange.getMessage().getHeaders().putAll(httpHeaderService.messageToHeaders(multipartMessage.getHeaderContent()));
			exchange.getMessage().setBody(responseString);
			break;
		}
		logger.info("Sending response to DataApp");

		headerCleaner.removeTechnicalHeaders(headerParts);
		exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());

		if (isEnabledWebSocket) {
			String responseMultipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, Boolean.TRUE);
			ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration
					.responseMessageBufferWebSocket();
			responseMessageServerBean.add(responseMultipartMessageString.getBytes());
		}
	}

}