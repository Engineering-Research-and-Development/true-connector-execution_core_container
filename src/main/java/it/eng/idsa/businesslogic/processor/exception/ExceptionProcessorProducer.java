package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.multipart.MultiPartMessage.Builder;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ExceptionProcessorProducer implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		exchange.getOut().setBody(exception.getMessage());
		Builder builder = new MultiPartMessage.Builder();
		MultiPartMessageServiceImpl multiPartMessageServiceImpl=new MultiPartMessageServiceImpl();
		String message = multiPartMessageServiceImpl.getHeader(exception.getMessage());
		builder.setHeader(message);
		MultiPartMessage builtMessage = builder.build();
		String stringMessage = MultiPart.toString(builtMessage, false);
		exchange.getOut().setBody(stringMessage);
		exchange.getOut().setHeader("Content-Type", builtMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed"));
		
	}

}
