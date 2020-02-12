package it.eng.idsa.businesslogic.processor.consumer;

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
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.multipart.MultiPartMessage.Builder;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerValidateTokenProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ConsumerValidateTokenProcessor.class);
	
	@Autowired
	DapsServiceImpl dapsServiceImpl;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message message = null;
		
		// Get "multipartMessageParts" from the input "exchange"
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		message = multiPartMessageServiceImpl.getMessage(multipartMessageParts.get("header"));
		
		// Get "token" from the input "multipartMessageParts"
		String token = multiPartMessageServiceImpl.getToken(multipartMessageParts.get("header").toString());
		logger.info("token: ", token);
		
		// Check is "token" valid
		boolean isTokenValid = dapsServiceImpl.validateToken(token);
//		boolean isTokenValid = true;
		
		if(isTokenValid==false) {			
			logger.error("Token is invalid");
			Message rejectionToken = multiPartMessageServiceImpl.createRejectionToken(message);
			Builder builder = new MultiPartMessage.Builder();
			builder.setHeader(rejectionToken);
			MultiPartMessage builtMessage = builder.build();
			String stringMessage = MultiPart.toString(builtMessage, false);
			throw new ExceptionForProcessor(stringMessage);
		}
		
		logger.info("is token valid: "+isTokenValid);
		multipartMessageParts.put("isTokenValid", isTokenValid);
		// Return multipartMessageParts
		exchange.getOut().setBody(multipartMessageParts);
	}

}
