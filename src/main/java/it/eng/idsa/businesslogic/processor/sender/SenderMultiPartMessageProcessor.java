package it.eng.idsa.businesslogic.processor.sender;

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
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SenderMultiPartMessageProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderMultiPartMessageProcessor.class);
	
	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		String header;
		String payload;
		String frowardTo;
		Map<String, Object> multipartMessageParts = new HashMap<String, Object>();
		Message message=null;
		
		// Get multipart message from the input "exchange"
		String multipartMessage = exchange.getMessage().getBody(String.class);
		
		try {
			// Create multipart message parts
			frowardTo=getForwardTo(multipartMessage);
			multipartMessageParts.put("frowardTo", frowardTo);
			header=multipartMessageService.getHeaderContentString(multipartMessage);
			multipartMessageParts.put("header", header);
			payload=multipartMessageService.getPayloadContent(multipartMessage);
			multipartMessageParts.put("payload", payload);
			message=multipartMessageService.getMessage(multipartMessage);
			multipartMessageParts.put("message", message);
		
			// Return multipartMessageParts
			exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());
			exchange.getMessage().setBody(multipartMessageParts);
		} catch (Exception e) {			
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_COMMON, 
					message);
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
