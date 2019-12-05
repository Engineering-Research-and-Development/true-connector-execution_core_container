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
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.impl.DapsServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerGetTokenFromDapsProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ProducerGetTokenFromDapsProcessor.class);
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private DapsServiceImpl dapsServiceImpl;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = exchange.getIn().getHeaders();
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		Message message=null;
		
		// Get message id
		try {
			message=multiPartMessageServiceImpl.getMessage(multipartMessageParts.get("header"));
			logger.info("message id=" + message.getId());
		}catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			Message rejectionMessageLocalIssues = multiPartMessageServiceImpl
					.createRejectionMessageLocalIssues(message);
			throw new ExceptionForProcessor(SerializationHelper.getInstance().toJsonLD(rejectionMessageLocalIssues));

		}
		if (message==null) {
			logger.error("Parsed multipart message is null");
			Message rejectionMessageLocalIssues = multiPartMessageServiceImpl
					.createRejectionMessageLocalIssues(message);
			throw new ExceptionForProcessor(SerializationHelper.getInstance().toJsonLD(rejectionMessageLocalIssues));
		}
		
		// Get the Token from the DAPS
		String token="";
		try {
			token=dapsServiceImpl.getJwtToken();
//			token="123";
		}catch (Exception e) {
			logger.error("Can not get the toke from the DAPS server");
			Message rejectionTokenLocalIssues = multiPartMessageServiceImpl
					.createRejectionTokenLocalIssues(message);
			throw new ExceptionForProcessor(SerializationHelper.getInstance().toJsonLD(rejectionTokenLocalIssues));
		}
		if (token.isEmpty()) {
			logger.error("The toke from the DAPS server is empty");
			Message rejectionTokenLocalIssues = multiPartMessageServiceImpl
					.createRejectionTokenLocalIssues(message);
			throw new ExceptionForProcessor(SerializationHelper.getInstance().toJsonLD(rejectionTokenLocalIssues));
		}
		
		logger.info("token=" + token);
		String messageStringWithToken=multiPartMessageServiceImpl.addToken(message, token);
		logger.info("messageStringWithToken=" + messageStringWithToken);
	
		multipartMessageParts.put("messageWithToken", messageStringWithToken);
		
		// Return exchange
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(multipartMessageParts);
		
	}

}
