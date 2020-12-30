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

import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SenderSendDataToBusinessLogicReceiver implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderSendDataToBusinessLogicReceiver.class);
	
	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private CommunicationService communicationMessageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		String header;
		String payload;
		String forwardTo;
	
		
		// Get "multipartMessageParts" from the input "exchange"
		Map<String, Object> multipartMessageParts = exchange.getMessage().getBody(HashMap.class);
		
		header = multipartMessageParts.get("header").toString();
		payload = multipartMessageParts.get("payload").toString();
		forwardTo = multipartMessageParts.get("frowardTo").toString();
		
		HttpEntity entity = multipartMessageService.createMultipartMessage(
				header,
				payload,
				null,
				ContentType.DEFAULT_TEXT);

		String response = communicationMessageService.sendData(forwardTo, entity);
		
		if (response==null) {
			logger.info("...communication error");
			//multiPartMessageServiceImpl.createRejectionCommunicationLocalIssues(message);
		}
		else {
			logger.info("data sent to destination "+forwardTo);
			logger.info("response "+response);
		}
	}

}
