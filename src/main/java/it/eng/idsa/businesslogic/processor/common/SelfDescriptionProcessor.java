package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.SelfDescriptionService;

@Component
public class SelfDescriptionProcessor implements Processor {

	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Override
	public void process(Exchange exchange) throws Exception {
		String selfDescription = selfDescriptionService.getConnectorSelfDescription();
		exchange.getMessage().setBody(selfDescription, String.class);
	}
}
