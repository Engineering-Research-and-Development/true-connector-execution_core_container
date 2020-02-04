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
import it.eng.idsa.businesslogic.service.impl.CommunicationServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.multipart.MultiPartMessage.Builder;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerSendDataToBusinessLogicProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ProducerSendDataToBusinessLogicProcessor.class);
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private CommunicationServiceImpl communicationMessageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = exchange.getIn().getHeaders();
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		
		String messageWithToken = multipartMessageParts.get("messageWithToken").toString();
		String header = multipartMessageParts.get("header").toString();
		String payload = multipartMessageParts.get("payload").toString();
		
		String multipartMessageJsonString = multiPartMessageServiceImpl.createMultipartMessageJson(messageWithToken, payload);
		
		String forwardTo = headesParts.get("Forward-To").toString();
		Message message = multiPartMessageServiceImpl.getMessage(header);
		
		String response = communicationMessageService.sendData(forwardTo, multipartMessageJsonString);
		
		if (response==null) {
			logger.info("...communication error");
			Message rejectionCommunicationLocalIssues = multiPartMessageServiceImpl.createRejectionMessage(message);
			Builder builder = new MultiPartMessage.Builder();
			builder.setHeader(rejectionCommunicationLocalIssues);
			MultiPartMessage builtMessage = builder.build();
			String stringMessage = MultiPart.toString(builtMessage, false);
			exchange.getOut().setBody(stringMessage);
			exchange.getOut().setHeader("Content-Type", builtMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed"));
		}
		else {
			logger.info("data sent to destination "+forwardTo);
			logger.info("response "+response);
			MultiPartMessage multiPartMessage = MultiPart.parseString(response);
			String multipartMessageString = MultiPart.toString(multiPartMessage, false);
			String contentType = multiPartMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
			exchange.getOut().setBody(multipartMessageString);
			exchange.getOut().setHeader("Content-Type", contentType);
		}
		
	}

}
