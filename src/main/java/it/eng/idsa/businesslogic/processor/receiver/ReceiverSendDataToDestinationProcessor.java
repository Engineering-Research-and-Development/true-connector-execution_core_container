package it.eng.idsa.businesslogic.processor.receiver;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ReceiverSendDataToDestinationProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ReceiverSendDataToDestinationProcessor.class);
	
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private CommunicationService communicationService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		// Get "multipartMessageParts" from the input "exchange"
		Map<String, Object> multipartMessageParts = exchange.getMessage().getBody(HashMap.class);
		// Get "isTokenValid" from the input "multipartMessageParts"
		Boolean isTokenValid = Boolean.valueOf(multipartMessageParts.get("isTokenValid").toString());
		logger.info("isTokenValid="+isTokenValid);
		
		Message message = null;
		if(isTokenValid) {
			logger.info("token is valid");
			message = (Message) multipartMessageParts.get("message");
			String payload = multipartMessageParts.get("payload").toString();
			String headerWithoutToken=multipartMessageService.removeToken(message);
			HttpEntity entity = multipartMessageService.createMultipartMessage(headerWithoutToken,payload, null,ContentType.DEFAULT_TEXT);
			String response = communicationService.sendData("http://"+configuration.getActivemqAddress()+"/api/message/outcoming?type=queue", entity);
			if (response==null) {
				logger.info("...communication error");
				rejectionMessageService.sendRejectionMessage(
						RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, 
						message);
			}
			logger.info("data sent to Data App");
			logger.info("response "+response);
			
			// Return multipartMessageParts
			exchange.getMessage().setBody(multipartMessageParts);
		} else {
			logger.error("Token is not valid");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_TOKEN, 
					message);
		}
	}
}
