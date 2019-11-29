package it.eng.idsa.businesslogic.processor.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.exception.ProcessorException;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerParseReceivedDataProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ProducerParseReceivedDataProcessor.class);

	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		String contentType;
		String forwardTo;
		String header;
		String payload;
		Message message=null;
		Map<String, Object> headesParts = new HashMap();
		Map<String, Object> multipartMessageParts = new HashMap();
		
		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getIn().getHeaders();
//		Object body = exchange.getIn().getBody(); 
		String receivedDataBody= exchange.getIn().getBody(String.class);
		if(receivedDataBody == null) {
			logger.error("Multipart message is null");
			throw new ProcessorException("Multipart message is null");
		}
		try {
			
			// Create headers parts
			contentType = receivedDataHeader.get("Content-Type").toString();
			headesParts.put("Content-Type", contentType);
			forwardTo = receivedDataHeader.get("Forward-To").toString();
			headesParts.put("Forward-To", forwardTo);
			
			// Create multipart message parts
			header=multiPartMessageServiceImpl.getHeader(receivedDataBody);
			multipartMessageParts.put("header", header);
			payload=multiPartMessageServiceImpl.getPayload(receivedDataBody);
			multipartMessageParts.put("payload", payload);
			
			// Return exchange
			exchange.getOut().setHeaders(headesParts);
			exchange.getOut().setBody(multipartMessageParts);
			
		} catch (Exception e) {
			multiPartMessageServiceImpl.createRejectionMessage(message);
			logger.error("Error parsing multipart message:" + e);
			throw new ProcessorException("Error parsing multipart message: " + e);
		}
		
		
	}

}
