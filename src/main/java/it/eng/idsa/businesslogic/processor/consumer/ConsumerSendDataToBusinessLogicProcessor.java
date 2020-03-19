package it.eng.idsa.businesslogic.processor.consumer;

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
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.ResponseMessageBufferBean;
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

	@Value("${application.isEnabledClearingHouse}")
	private boolean isEnabledClearingHouse;
	
	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private WebSocketServerConfiguration webSocketServerConfiguration;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = new HashMap();
		
		Map<String, Object> multipartMessagePartsReceived = exchange.getIn().getBody(HashMap.class);
		
		// Put in the header value of the application.property: application.isEnabledClearingHouse
		headesParts.put("Is-Enabled-Clearing-House", isEnabledClearingHouse);
		
		// Get header, payload and message
		String header = multipartMessagePartsReceived.get("header").toString();
		String payload = null;
		if(multipartMessagePartsReceived.containsKey("payload")) {
			payload = multipartMessagePartsReceived.get("payload").toString();
		}
		
		// Prepare multipart message as string
		HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(header, payload, null);
		String responseString = EntityUtils.toString(entity, "UTF-8");
		
		// Return exchange
		MultiPartMessage multiPartMessage = MultiPart.parseString(responseString);
		String multipartMessageString = MultiPart.toString(multiPartMessage, false);
		String contentType = multiPartMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
		headesParts.put("Content-Type", contentType);
		
		// TODO: Send The MultipartMessage message to the WebaSocket
		if(isEnabledIdscp) {
			ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration.responseMessageBufferWebSocket();
			responseMessageServerBean.add(multipartMessageString.getBytes());
		}
		
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(multipartMessageString);
	}	
}
