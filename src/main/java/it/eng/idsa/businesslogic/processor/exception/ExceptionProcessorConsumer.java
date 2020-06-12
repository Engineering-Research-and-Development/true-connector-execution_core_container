package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class ExceptionProcessorConsumer implements Processor {
	
	@Autowired
	MultipartMessageService multipartMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		String message = multipartMessageService.getHeaderContentString(exception.getMessage());
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(message)
    			.build();
    	String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
		
		exchange.getOut().setBody(multipartMessageString);
		exchange.getOut().setHeader("header", multipartMessageString);
		exchange.getOut().setHeader("payload", "RejectionMessage");

		
	}

}
