package it.eng.idsa.businesslogic.processor.receiver;

import java.util.HashMap;
import java.util.Map;

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
public class ReceiverExceptionMultiPartMessageProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ReceiverExceptionMultiPartMessageProcessor.class);
	
	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		String header;
		String payload;
		Message message=null;
		Map<String, Object> headesParts = new HashMap<String, Object>();
		Map<String, Object> multipartMessageParts = new HashMap<String, Object>();
		
		if(!exchange.getMessage().getHeaders().containsKey("header"))	{
			logger.error("Multipart message header or/and payload is null");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_COMMON, 
					message);
		}
		try {
			// Create headers parts
			header= multipartMessageService.getHeaderContentString(exchange.getMessage().getHeader("header").toString());
			multipartMessageParts.put("header", header);
			if(exchange.getMessage().getHeaders().containsKey("payload")) {
				payload=exchange.getMessage().getHeader("payload").toString();
				multipartMessageParts.put("payload", payload);
			}
			message=multipartMessageService.getMessage(multipartMessageParts.get("header"));
			
			// Return exchange
			exchange.getMessage().setHeaders(headesParts);
			exchange.getMessage().setBody(multipartMessageParts);
			
		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_COMMON, 
					message);
		}
	}

}
