package it.eng.idsa.businesslogic.processor.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.impl.DapsServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class GetTokenFromDapsProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(GetTokenFromDapsProcessor.class);
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageService;
	
	@Autowired
	private DapsServiceImpl dapsServiceImpl;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = exchange.getIn().getHeaders();
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		Message message=null;
		
		// Get message id
		try {
			message=multiPartMessageService.getMessage(multipartMessageParts.get("header"));
			logger.info("message id=" + message.getId());
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			multiPartMessageService.createRejectionMessageLocalIssues(message);

		}
		if (message==null) {
			multiPartMessageService.createRejectionMessageLocalIssues(message);
		}
		
		// Get the Token from the DAPS
		String token="";
		try {
			token=dapsServiceImpl.getJwtToken();
//			token="123";
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			multiPartMessageService.createRejectionTokenLocalIssues(message);
		}
		if (token.isEmpty()) 
			multiPartMessageService.createRejectionTokenLocalIssues(message);
		
		logger.info("token=" + token);
		String messageStringWithToken=multiPartMessageService.addToken(message, token);
		logger.info("messageStringWithToken=" + messageStringWithToken);
	
		multipartMessageParts.put("messageWithToken", messageStringWithToken);
		
		// Return exchange
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(multipartMessageParts);
		
	}

}
