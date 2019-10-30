package it.eng.idsa.businesslogic.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.exception.ProcessorException;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

@Component
public class MultiPartMessageProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(MultiPartMessageProcessor.class);

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		String header;
		String payload;
		Message message=null;
		Map<String, Object> multipartMessageParts = new HashMap();
		
		// Get multipart message from the input "exchange"
		String multipartMessage = exchange.getIn().getBody(String.class);
		if(multipartMessage == null) {
			logger.error("Multipart message is null");
			throw new ProcessorException("Multipart message is null");
		}
		try {
			// Create multipart message parts
			header=multiPartMessageServiceImpl.getHeader(multipartMessage);
			multipartMessageParts.put("header", header);
			payload=multiPartMessageServiceImpl.getPayload(multipartMessage);
			multipartMessageParts.put("payload", payload);
			message=multiPartMessageServiceImpl.getMessage(multipartMessage);
			multipartMessageParts.put("message", message);
			// Return multipartMessageParts
			exchange.getOut().setBody(multipartMessageParts);
		} catch (Exception e) {
			multiPartMessageServiceImpl.createRejectionMessage(message);
			logger.error("Error parsing multipart message:" + e);
			throw new ProcessorException("Error parsing multipart message: " + e);
		}
	}
	
}
