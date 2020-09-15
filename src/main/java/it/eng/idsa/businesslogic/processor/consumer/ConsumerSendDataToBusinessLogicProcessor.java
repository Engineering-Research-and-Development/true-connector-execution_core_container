package it.eng.idsa.businesslogic.processor.consumer;

import java.util.HashMap;
import java.util.Map;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.processor.consumer.websocket.server.ResponseMessageBufferBean;
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
public class ConsumerSendDataToBusinessLogicProcessor implements Processor {

	@Value("${application.isEnabledClearingHouse}")
	private boolean isEnabledClearingHouse;

	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Autowired(required = false)
	private WebSocketServerConfigurationB webSocketServerConfiguration;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headersParts = exchange.getIn().getHeaders();

		Map<String, Object> multipartMessagePartsReceived = exchange.getIn().getBody(HashMap.class);

		// Get header, payload and message
		String header = multipartMessagePartsReceived.get("header").toString();
		String payload = null;
		if (multipartMessagePartsReceived.containsKey("payload")) {
			payload = multipartMessagePartsReceived.get("payload").toString();
		}

		MultipartMessage responseMessage = new MultipartMessageBuilder().withHeaderContent(header)
				.withPayloadContent(payload).build();
		String responseString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);

		String contentType = responseMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed");
		headersParts.put("Content-Type", contentType);

		// TODO: Send The MultipartMessage message to the WebaSocket
		if (isEnabledIdscp || isEnabledWebSocket) { // TODO Try to remove this config property
			ResponseMessageBufferBean responseMessageServerBean = webSocketServerConfiguration
					.responseMessageBufferWebSocket();
			responseMessageServerBean.add(responseString.getBytes());
		}
		HeaderCleaner.removeTechnicalHeaders(headersParts);
		if (isEnabledClearingHouse) {
			// Put in the header value of the application.property:
			// application.isEnabledClearingHouse
			headersParts.put("Is-Enabled-Clearing-House", isEnabledClearingHouse);
		}
		if (isEnabledWebSocket) {
			headersParts.put("Is-Enabled-DataApp-WebSocket", isEnabledWebSocket);
		}
		exchange.getOut().setHeaders(headersParts);
		exchange.getOut().setBody(responseString);
	}
}
