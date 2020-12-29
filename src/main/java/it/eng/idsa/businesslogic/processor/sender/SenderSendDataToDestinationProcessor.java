package it.eng.idsa.businesslogic.processor.sender;

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
public class SenderSendDataToDestinationProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderSendDataToDestinationProcessor.class);

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

		Map<String, Object> headesParts = exchange.getMessage().getHeaders();
		Map<String, Object> multipartMessageParts = exchange.getMessage().getBody(HashMap.class);
		
		String messageWithToken = multipartMessageParts.get("messageWithToken").toString();
		String header = multipartMessageParts.get("header").toString();
		String payload = multipartMessageParts.get("payload").toString();
		String forwardTo = headesParts.get("Forward-To").toString();
		Message message = multipartMessageService.getMessage(header);

		HttpEntity entity = multipartMessageService.createMultipartMessage(messageWithToken, payload, forwardTo,ContentType.DEFAULT_TEXT);
		String response = communicationService.sendData("http://"+configuration.getActivemqAddress()+"/api/message/incoming?type=queue", entity);
		
		if (response==null) {
			logger.info("...communication error");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, 
					message);
		}
		else {
			logger.info("data sent to destination "+forwardTo);
			logger.info("response "+response);
		}
	}

}
