package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ExceptionProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		exchange.getOut().setBody(exception.getMessage());
	}

}
