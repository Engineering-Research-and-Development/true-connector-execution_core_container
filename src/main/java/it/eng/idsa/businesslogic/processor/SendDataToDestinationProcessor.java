package it.eng.idsa.businesslogic.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.exception.ProcessorException;
import it.eng.idsa.businesslogic.service.impl.CommunicationServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SendDataToDestinationProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(SendDataToDestinationProcessor.class);
	
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private CommunicationServiceImpl communicationServiceImpl;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		// Get "multipartMessageParts" from the input "exchange"
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		// Get "isTokenValid" from the input "multipartMessageParts"
		Boolean isTokenValid = Boolean.valueOf(multipartMessageParts.get("isTokenValid").toString());
		logger.info("isTokenValid="+isTokenValid);
		
		if(isTokenValid) {
			logger.info("token is valid");
			Message message = (Message) multipartMessageParts.get("message");
			String payload = multipartMessageParts.get("payload").toString();
			String headerWithoutToken=multiPartMessageServiceImpl.removeToken(message);
			HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(headerWithoutToken,payload);
			String response = communicationServiceImpl.sendData("http://"+configuration.getActivemqAddress()+"/api/message/outcoming?type=queue", entity);
			if (response==null) {
				logger.info("...communication error");
				throw new ProcessorException("Communication error");
			}
			logger.info("data sent to Data App");
			logger.info("response "+response);
			
			// Return multipartMessageParts
			exchange.getOut().setBody(multipartMessageParts);
		} else {
			logger.error("Token is not valid");
			throw new ProcessorException("Token is not valid");
		}
	}
}
