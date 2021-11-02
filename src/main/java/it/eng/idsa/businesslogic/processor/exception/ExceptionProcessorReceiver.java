package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

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
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		String message = MultipartMessageProcessor.parseMultipartMessage(exception.getMessage()).getHeaderContentString();
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(message)
    			.build();
		
		exchange.getMessage().setBody(multipartMessage);
	}
}
