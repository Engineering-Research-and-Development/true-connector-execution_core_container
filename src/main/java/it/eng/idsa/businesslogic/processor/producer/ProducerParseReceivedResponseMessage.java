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
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerParseReceivedResponseMessage implements Processor {

	private static final Logger logger = LogManager.getLogger(ProducerParseReceivedResponseMessage.class);

	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		String header;
		String payload;
		Message message=null;
		Map<String, Object> multipartMessageParts = new HashMap<String, Object>();
		
		// Get multipart message from the input "exchange"
		String multipartMessage = exchange.getIn().getBody(String.class);
		if(multipartMessage == null) {
			logger.error("Multipart message is null");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_COMMON, 
					message);
		}
		try {
			// Create multipart message parts
			header=multipartMessageService.getHeaderContentString(multipartMessage);
			multipartMessageParts.put("header", header);
			if(multipartMessageService.getPayloadContent(multipartMessage)!=null) {
				payload=multipartMessageService.getPayloadContent(multipartMessage);
				multipartMessageParts.put("payload", payload);
			}
			message=multipartMessageService.getMessage(multipartMessageParts.get("header"));
			
			exchange.getMessage().setHeaders(exchange.getIn().getHeaders());
			// Return multipartMessageParts
			exchange.getMessage().setBody(multipartMessageParts);
		} catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_COMMON, 
					message);
			
		}
	}

}
