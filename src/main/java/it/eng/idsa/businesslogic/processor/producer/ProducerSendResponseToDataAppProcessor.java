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
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

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
    private MultipartMessageService multipartMessageService;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Autowired(required = false)
	WebSocketServerConfigurationA webSocketServerConfiguration;

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
		String responseMultipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
		
		String contentType = multipartMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
		headesParts.put("Content-Type", contentType);
		
		if(!isEnabledClearingHouse) {
			// clear from Headers multipartMessageBody (it is not unusable for the Open Data App)
			Map<String, Object> headers = exchange.getIn().getHeaders();
			headers.remove("multipartMessageBody");
		}

		// TODO: Send The MultipartMessage message to the WebSocket
		if(isEnabledWebSocket) {
			ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration.responseMessageBufferWebSocket();
			responseMessageServerBean.add(responseMultipartMessageString.getBytes());
		}
		HeaderCleaner.removeTechnicalHeaders(headesParts);
		exchange.getOut().setHeaders(headesParts);
		exchange.getOut().setBody(responseMultipartMessageString);
	}	

	private String filterHeader(String header) throws JsonMappingException, JsonProcessingException {
		Message message = multipartMessageService.getMessage(header);
		return multipartMessageService.removeToken(message);
	}
	
	private String filterRejectionMessageHeader(String header) throws JsonMappingException, JsonProcessingException {
		Message message = multipartMessageService.getMessage(header);
		return multipartMessageService.removeToken(message);
	}
	
	
	
}