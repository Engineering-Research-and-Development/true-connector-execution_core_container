package it.eng.idsa.businesslogic.processor.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerSendResponseToDataAppProcessor implements Processor {
	
	@Value("${application.isEnabledClearingHouse}")
	private boolean isEnabledClearingHouse;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = new HashMap();
		
		Map<String, Object> multipartMessagePartsReceived = exchange.getIn().getBody(HashMap.class);
		
		// Put in the header value of the application.property: application.isEnabledClearingHouse
		headesParts.put("Is-Enabled-Clearing-House", isEnabledClearingHouse);
		
		String header = null;
		String payload = null;
		if(multipartMessagePartsReceived.get("payload")!=null) {
			payload = multipartMessagePartsReceived.get("payload").toString();
			if(payload.equals("RejectionMessage\n")) {
				header = this.filterRejectionMessageHeader(multipartMessagePartsReceived.get("header").toString());
				payload = null;
			}else {
				header = this.filterHeader(multipartMessagePartsReceived.get("header").toString());
				payload = multipartMessagePartsReceived.get("payload").toString();
			}
		} else {
			header = this.filterHeader(multipartMessagePartsReceived.get("header").toString());
		}
		
		// Prepare multipart message as string
		HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(header, payload, null);
		String responseString = EntityUtils.toString(entity, "UTF-8");
		
		// Return exchange
		MultiPartMessage multiPartMessage = MultiPart.parseString(responseString);
		String multipartMessageString = MultiPart.toString(multiPartMessage, false);
		String contentType = multiPartMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
		headesParts.put("Content-Type", contentType);
		
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(multipartMessageString);
	}	

	private String filterHeader(String header) throws JsonMappingException, JsonProcessingException {
		Message message = multiPartMessageServiceImpl.getMessage(header);
		return multiPartMessageServiceImpl.removeToken(message);
	}
	
	private String filterRejectionMessageHeader(String header) throws JsonMappingException, JsonProcessingException {
		Message message = multiPartMessageServiceImpl.getMessage(header);
		return multiPartMessageServiceImpl.removeToken(message);
	}
	
}
