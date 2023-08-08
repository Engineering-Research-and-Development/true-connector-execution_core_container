package it.eng.idsa.businesslogic.processor.sender.registration;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.util.TrueConnectorConstants;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

public abstract class AbstractCreateRegistrationMessage implements Processor {
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getMessage().getHeaders();
		String connector = selfDescriptionService.getConnectorSelfDescription();
		Message connectorAvailable = getConnectorMessage();

		MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(connectorAvailable)
					.withPayloadContent(connector)
					.withPayloadHeader(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString()))
					.build();
		
		// Return exchange
		exchange.getMessage().setBody(multipartMessage);
		publishEvent(multipartMessage, (String) receivedDataHeader.get(TrueConnectorConstants.CORRELATION_ID));
	}

	abstract Message getConnectorMessage();
	
	abstract void publishEvent(MultipartMessage multipartMessage, String correlationId);
}
