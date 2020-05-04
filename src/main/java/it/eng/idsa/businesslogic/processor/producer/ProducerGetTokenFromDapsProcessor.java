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
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerGetTokenFromDapsProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ProducerGetTokenFromDapsProcessor.class);
	
	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private DapsService dapsService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = exchange.getIn().getHeaders();
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		Message message=null;
		
		// Get message id
		try {
			message=multipartMessageService.getMessage(multipartMessageParts.get("header"));
			logger.info("message id=" + message.getId());
		}catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, 
					message);
		}
		if (message==null) {
			logger.error("Parsed multipart message is null");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, 
					message);
		}
		
		// Get the Token from the DAPS
		String token="";
		try {
			token=dapsService.getJwtToken();
//			token="123";
		}catch (Exception e) {
			logger.error("Can not get the token from the DAPS server " + e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_TOKEN_LOCAL_ISSUES, 
					message);
		}
		
		if(token==null) {
			logger.error("Can not get the token from the DAPS server");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, 
					message);
		}
		
		if (token.isEmpty()) {
			logger.error("The token from the DAPS server is empty");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_TOKEN_LOCAL_ISSUES, 
					message);
		}
		
		logger.info("token=" + token);
		String messageStringWithToken=multipartMessageService.addToken(message, token);
		logger.info("messageStringWithToken=" + messageStringWithToken);
	
		multipartMessageParts.put("messageWithToken", messageStringWithToken);
		
		// Return exchange
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(multipartMessageParts);
		
	}

}
