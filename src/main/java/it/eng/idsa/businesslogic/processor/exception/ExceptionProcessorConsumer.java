package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.multipart.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ExceptionProcessorConsumer implements Processor {
	
	@Autowired
	MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
    MultipartMessageService multipartMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		exchange.getOut().setBody(exception.getMessage());
		String message = multiPartMessageServiceImpl.getHeaderContentString(exception.getMessage());
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(message)
    			.build();
    	String multipartMessageString = multipartMessageService.multipartMessagetoString(multipartMessage, false);
		
		exchange.getOut().setHeader("header", multipartMessageString);
		exchange.getOut().setHeader("payload", "RejectionMessage");
		
	}

}
