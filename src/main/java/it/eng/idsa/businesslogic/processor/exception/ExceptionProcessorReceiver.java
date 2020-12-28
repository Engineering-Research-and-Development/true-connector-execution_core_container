package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
public class ExceptionProcessorReceiver implements Processor {
	
	@Autowired
	MultipartMessageService multipartMessageService;
	
	@Autowired
	private HttpHeaderService headerService;
	
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
			// empty body of response since it will contain Message
			exchange.getMessage().setBody(null);
			exchange.getMessage().setHeaders(headerService.prepareMessageForSendingAsHttpHeaders(multipartMessage));
		} else {
			String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
			
			exchange.getMessage().setBody(multipartMessageString);
			exchange.getMessage().setHeader("header", multipartMessageString);
			exchange.getMessage().setHeader("payload", "RejectionMessage");
		}
	}
}
