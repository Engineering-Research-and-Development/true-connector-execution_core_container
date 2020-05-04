package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.multipart.service.MultipartMessageTransformerService;
import it.eng.idsa.businesslogic.service.impl.MultipartMessageServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ExceptionProcessorConsumer implements Processor {
	
	@Autowired
	MultipartMessageServiceImpl multipartMessageServiceImpl;
	
	@Autowired
    MultipartMessageTransformerService multipartMessageTransformerService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		String message = multipartMessageServiceImpl.getHeaderContentString(exception.getMessage());
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(message)
    			.build();
    	String multipartMessageString = multipartMessageTransformerService.multipartMessagetoString(multipartMessage, false);
		
		exchange.getOut().setBody(multipartMessageString);
		exchange.getOut().setHeader("header", multipartMessageString);
		exchange.getOut().setHeader("payload", "RejectionMessage");

		
	}

}
