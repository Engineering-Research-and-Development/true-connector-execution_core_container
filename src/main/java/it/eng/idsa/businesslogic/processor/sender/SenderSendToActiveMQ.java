package it.eng.idsa.businesslogic.processor.sender;

import java.util.HashMap;
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
public class SenderSendToActiveMQ implements Processor {
    
    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {
        
    	// Get exchange
        Map<String, Object> headersParts = exchange.getMessage().getHeaders();
        Map<String, Object> multipartMessageParts = exchange.getMessage().getBody(HashMap.class);
        
        Map<String, Object> queueContent = new HashMap();
        queueContent.put("headersParts", headersParts);
        queueContent.put("multipartMessageParts", multipartMessageParts);
        
        jmsTemplate.convertAndSend(Application.QUEUE_INCOMING, queueContent);
        
    }

}
