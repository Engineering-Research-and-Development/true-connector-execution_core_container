package it.eng.idsa.businesslogic.processor.producer;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerMultiPartMessageProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ProducerMultiPartMessageProcessor.class);
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		String header;
		String payload;
		String frowardTo;
		Map<String, Object> multipartMessageParts = new HashMap();
		Message message=null;
		
		// Get multipart message from the input "exchange"
		String multipartMessage = exchange.getIn().getBody(String.class);
		
		try {
			// Create multipart message parts
			frowardTo=getForwardTo(multipartMessage);
			multipartMessageParts.put("frowardTo", frowardTo);
			header=multiPartMessageServiceImpl.getHeader(multipartMessage);
			multipartMessageParts.put("header", header);
			payload=multiPartMessageServiceImpl.getPayload(multipartMessage);
			multipartMessageParts.put("payload", payload);
			message=multiPartMessageServiceImpl.getMessage(multipartMessage);
			multipartMessageParts.put("message", message);
		
			// Return multipartMessageParts
			exchange.getOut().setBody(multipartMessageParts);
		} catch (Exception e) {
			Message rejectionMessage = multiPartMessageServiceImpl.createRejectionMessage(message);
			logger.error("Error parsing multipart message:" + e);
			throw new ExceptionForProcessor(SerializationHelper.getInstance().toJsonLD(rejectionMessage));
		}
	}
	
	private String getForwardTo(String multipartMessage) {
		String frowardTo = null;
		Scanner scanner = new Scanner(multipartMessage);
		int i=0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			i++;
			if(i==5) frowardTo = line;
			}
		scanner.close();
		return frowardTo;
		
	}

}
