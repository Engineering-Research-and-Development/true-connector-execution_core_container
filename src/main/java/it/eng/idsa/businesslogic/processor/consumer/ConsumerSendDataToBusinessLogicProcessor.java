package it.eng.idsa.businesslogic.processor.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;
import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.multipart.service.MultipartMessageService;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

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

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private MultipartMessageService multiPartMessageService;
	
	@Autowired
	private WebSocketServerConfiguration webSocketServerConfiguration;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = new HashMap<String, Object>();
		
		Map<String, Object> multipartMessagePartsReceived = exchange.getIn().getBody(HashMap.class);
		
		// Put in the header value of the application.property: application.isEnabledClearingHouse
		headesParts.put("Is-Enabled-Clearing-House", isEnabledClearingHouse);
		
		// Get header, payload and message
		String header = multipartMessagePartsReceived.get("header").toString();
		String payload = null;
		if(multipartMessagePartsReceived.containsKey("payload")) {
			payload = multipartMessagePartsReceived.get("payload").toString();
		}
		
		MultipartMessage responseMessage = new MultipartMessageBuilder()
				.withHeaderContent(header)
				.withPayloadContent(payload)
				.build();
		String responseString = multiPartMessageService.multipartMessagetoString(responseMessage, false);
		
		String contentType = responseMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
		headesParts.put("Content-Type", contentType);
		
		// TODO: Send The MultipartMessage message to the WebaSocket
		if(isEnabledIdscp || isEnabledWebSocket) { //TODO Try to remove this config property
			ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration.responseMessageBufferWebSocket();
			responseMessageServerBean.add(responseString.getBytes());
		}
		
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(responseString);
	}	
}
