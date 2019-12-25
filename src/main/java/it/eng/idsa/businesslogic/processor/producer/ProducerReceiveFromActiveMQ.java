package it.eng.idsa.businesslogic.processor.producer;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.Application;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerReceiveFromActiveMQ implements Processor {

	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> queueContent = (Map<String, Object>) jmsTemplate.receiveAndConvert(Application.QUEUE_INCOMING);
		
		Map<String, Object> headesParts = (Map<String, Object>) queueContent.get("headesParts");
		Map<String, Object> multipartMessageParts = (Map<String, Object>) queueContent.get("multipartMessageParts");
		
		// Return exchange
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(multipartMessageParts);
		
	}

}
