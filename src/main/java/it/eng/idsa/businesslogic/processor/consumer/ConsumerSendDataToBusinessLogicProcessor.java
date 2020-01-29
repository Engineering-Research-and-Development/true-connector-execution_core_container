package it.eng.idsa.businesslogic.processor.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.domain.json.HeaderBodyJson;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerSendDataToBusinessLogicProcessor implements Processor {

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> multipartMessagePartsReceived = exchange.getIn().getBody(HashMap.class);
		
		// Get header, payload and message
		String header = multipartMessagePartsReceived.get("header").toString();
		String payload = multipartMessagePartsReceived.get("payload").toString();
		
		// Prepare multipart message as string
		HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(header, payload, null);
		String responseString = EntityUtils.toString(entity, "UTF-8");
		
		// Return exchange
		MultiPartMessage multiPartMessage = MultiPart.parseString(responseString);
		String multipartMessageString = MultiPart.toString(multiPartMessage, false);
		String contentType = multiPartMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
		exchange.getOut().setBody(multipartMessageString);
		exchange.getOut().setHeader("Content-Type", contentType);
	}	
}
