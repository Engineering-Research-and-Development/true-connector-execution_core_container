package it.eng.idsa.businesslogic.processor.receiver;

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
public class ReceiverReceiveFromActiveMQ implements Processor {
   
    @Autowired
    private JmsTemplate jmsTemplate;
    
    @Override
    public void process(Exchange exchange) throws Exception {
    	
        Map<String, Object> queueContent = (Map<String, Object>) jmsTemplate.receiveAndConvert(Application.QUEUE_OUTCOMING);
        
        Map<String, Object> headersParts = (Map<String, Object>) queueContent.get("headersParts");
        Map<String, Object> multipartMessageParts = (Map<String, Object>) queueContent.get("multipartMessageParts");
            
        // Return exchange
        exchange.getMessage().setHeaders(headersParts);
        exchange.getMessage().setBody(multipartMessageParts);
        
    }

}
