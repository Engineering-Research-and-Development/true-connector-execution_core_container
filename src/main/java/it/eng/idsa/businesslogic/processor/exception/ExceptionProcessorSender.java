package it.eng.idsa.businesslogic.processor.exception;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ExceptionProcessorSender implements Processor {

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired
	private HttpHeaderService headerService;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Autowired(required = false)
	private WebSocketServerConfigurationA webSocketServerConfiguration;

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Autowired
	private HeaderCleaner headerCleaner;

	@Override
	public void process(Exchange exchange) throws Exception {
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);

		if (exception instanceof org.apache.camel.CamelAuthorizationException) {
			exchange.getMessage().setBody("Access denied");
			exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatus.UNAUTHORIZED);
			exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "text/plain");
		} else {
			String message = MultipartMessageProcessor.parseMultipartMessage(exception.getMessage())
					.getHeaderContentString();
			MultipartMessage multipartMessage = new MultipartMessageBuilder().withHeaderContent(message).build();

			if (RouterType.HTTP_HEADER.equals(openDataAppReceiverRouter)) {
				// need to empty body here because it will contain response message received
				exchange.getMessage().setBody(null);
				exchange.getMessage().setHeaders(headerService.messageToHeaders(multipartMessage.getHeaderContent()));
			} else {
				String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage,
						false, Boolean.TRUE);
				headerCleaner.removeTechnicalHeaders(exchange.getMessage().getHeaders());
				String contentType = null;

				if (isEnabledDataAppWebSocket) {
					ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration
							.responseMessageBufferWebSocket();
					responseMessageServerBean.add(multipartMessageString.getBytes());
				}

				if (RouterType.MULTIPART_MIX.equals(openDataAppReceiverRouter)) {
					exchange.getMessage().setBody(multipartMessageString);
					Optional<String> boundary = MultipartMessageProcessor
							.getMessageBoundaryFromMessage(multipartMessageString);
					contentType = "multipart/mixed; boundary=" + boundary.orElse("---aaa") + ";charset=UTF-8";
					exchange.getMessage().setHeader("Content-Type", contentType);
				} else {
					HttpEntity resultEntity = null;

					resultEntity = multipartMessageService.createMultipartMessage(message, null, null,
							ContentType.MULTIPART_FORM_DATA);
					contentType = resultEntity.getContentType().getValue();
					exchange.getMessage().setBody(resultEntity.getContent().readAllBytes());
					exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, contentType);
				}
			}
		}
	}
}
