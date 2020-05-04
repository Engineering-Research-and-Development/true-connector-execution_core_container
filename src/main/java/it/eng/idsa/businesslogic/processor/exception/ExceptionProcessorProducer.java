package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.MultipartMessageTransformerService;

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
    private MultipartMessageTransformerService multipartMessageTransformerService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		exchange.getOut().setBody(exception.getMessage());
		String message = multipartMessageService.getHeaderContentString(exception.getMessage());
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(message)
    			.build();
    	String multipartMessageString = multipartMessageTransformerService.multipartMessagetoString(multipartMessage, false);
		
		exchange.getOut().setBody(multipartMessageString);
		String contentType = multipartMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
        exchange.getOut().setHeader("Content-Type", contentType);
		
	}

}
