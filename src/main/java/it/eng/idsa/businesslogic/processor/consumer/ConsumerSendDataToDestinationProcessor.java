package it.eng.idsa.businesslogic.processor.consumer;

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
import it.eng.idsa.businesslogic.service.impl.CommunicationServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerSendDataToDestinationProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ConsumerSendDataToDestinationProcessor.class);
	
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private RejectionMessageServiceImpl rejectionMessageServiceImpl;
	
	@Autowired
	private CommunicationServiceImpl communicationServiceImpl;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		// Get "multipartMessageParts" from the input "exchange"
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		// Get "isTokenValid" from the input "multipartMessageParts"
		Boolean isTokenValid = Boolean.valueOf(multipartMessageParts.get("isTokenValid").toString());
		logger.info("isTokenValid="+isTokenValid);
		
		Message message = null;
		if(isTokenValid) {
			logger.info("token is valid");
			message = (Message) multipartMessageParts.get("message");
			String payload = multipartMessageParts.get("payload").toString();
			String headerWithoutToken=multiPartMessageServiceImpl.removeToken(message);
			HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(headerWithoutToken,payload, null);
			String response = communicationServiceImpl.sendData("http://"+configuration.getActivemqAddress()+"/api/message/outcoming?type=queue", entity);
			if (response==null) {
				logger.info("...communication error");
				rejectionMessageServiceImpl.sendRejectionMessage(
						RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, 
						message);
			}
			logger.info("data sent to Data App");
			logger.info("response "+response);
			
			// Return multipartMessageParts
			exchange.getOut().setBody(multipartMessageParts);
		} else {
			logger.error("Token is not valid");
			rejectionMessageServiceImpl.sendRejectionMessage(
					RejectionMessageType.REJECTION_TOKEN, 
					message);
		}
	}
}
