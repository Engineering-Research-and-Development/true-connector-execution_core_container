package it.eng.idsa.businesslogic.processor.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.impl.CommunicationServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerSenDataToDestinationProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ProducerSenDataToDestinationProcessor.class);

	@Autowired
	private ApplicationConfiguration configuration;
	
//	@Autowired
//	private CommunicationServiceImpl communicationMessageService;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private CommunicationServiceImpl communicationServiceImpl;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headesParts = exchange.getIn().getHeaders();
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		
		String messageWithToken = multipartMessageParts.get("messageWithToken").toString();
		String header = multipartMessageParts.get("header").toString();
		String payload = multipartMessageParts.get("payload").toString();
		String forwardTo = headesParts.get("Forward-To").toString();
		Message message = multiPartMessageServiceImpl.getMessage(header);

		HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(messageWithToken, payload, forwardTo);
		String response = communicationServiceImpl.sendData("http://"+configuration.getActivemqAddress()+"/api/message/incoming?type=queue", entity);
		
		if (response==null) {
			logger.info("...communication error");
			multiPartMessageServiceImpl.createRejectionCommunicationLocalIssues(message);
		}
		else {
			logger.info("data sent to destination "+forwardTo);
			logger.info("response "+response);
		}
	}

}
