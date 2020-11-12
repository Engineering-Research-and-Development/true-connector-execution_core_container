package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ExceptionProcessorProducer implements Processor {

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired
	private HttpHeaderService headerService;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Autowired(required = false)
	WebSocketServerConfigurationA webSocketServerConfiguration;

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Override
	public void process(Exchange exchange) throws Exception {

		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		String message = multipartMessageService.getHeaderContentString(exception.getMessage());

		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(message)
				.build();

		if (openDataAppReceiverRouter.equals("http-header")) {
			exchange.getOut().setHeaders(headerService.prepareMessageForSendingAsHttpHeaders(multipartMessage));
		} else {
			String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);

			if (isEnabledDataAppWebSocket) {
				ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration
						.responseMessageBufferWebSocket();
				responseMessageServerBean.add(multipartMessageString.getBytes());
			}

			exchange.getMessage().setBody(multipartMessageString);
			String contentType = multipartMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
			exchange.getMessage().setHeader("Content-Type", contentType);
		}

	}

}
