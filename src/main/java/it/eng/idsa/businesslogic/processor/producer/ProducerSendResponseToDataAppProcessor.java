package it.eng.idsa.businesslogic.processor.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.multipart.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

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
	
	@Autowired
    private MultipartMessageService multipartMessageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = exchange.getIn().getHeaders();
		
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
		
		// Prepare response
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(header)
    			.withPayloadContent(payload)
    			.build();
		String responseMultipartMessageString = multipartMessageService.multipartMessagetoString(multipartMessage, false);
		
		String contentType = multipartMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
		headesParts.put("Content-Type", contentType);
		
		if(!isEnabledClearingHouse) {
			// clear from Headers multipartMessageBody (it is not unusable for the Open Data App)
			Map<String, Object> headers = exchange.getIn().getHeaders();
			headers.remove("multipartMessageBody");
		}
		
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(responseMultipartMessageString);
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
