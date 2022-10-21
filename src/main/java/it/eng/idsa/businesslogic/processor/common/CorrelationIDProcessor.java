package it.eng.idsa.businesslogic.processor.common;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.util.TrueConnectorConstants;

@Component
public class CorrelationIDProcessor implements Processor {
	
	@Override
	public void process(Exchange exchange) throws Exception {
		if(null == exchange.getMessage().getHeader(TrueConnectorConstants.CORRELATION_ID)) {
			String correlationId = UUID.randomUUID().toString();
			exchange.getMessage().getHeaders().put(TrueConnectorConstants.CORRELATION_ID, correlationId);
		}
	}
}
