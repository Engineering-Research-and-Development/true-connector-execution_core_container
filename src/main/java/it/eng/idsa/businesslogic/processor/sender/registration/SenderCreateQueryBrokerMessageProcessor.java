package it.eng.idsa.businesslogic.processor.sender.registration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class SenderCreateQueryBrokerMessageProcessor implements Processor {
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Override
	@CamelAuditable(successEventType = TrueConnectorEventType.CONNECTOR_BROKER_QUERY, 
	failureEventType = TrueConnectorEventType.EXCEPTION_BAD_REQUEST)
	public void process(Exchange exchange) throws Exception {
		String payload =  exchange.getMessage().getHeader("payload") != null 
				// case when request is multipart-form
				? (String) exchange.getMessage().getHeader("payload")
				// case when request is multipart-mixed
				: exchange.getMessage().getBody(String.class);
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(selfDescriptionService.getConnectorQueryMessage())
				.withPayloadContent(payload)
				.build();
		
		exchange.getMessage().setBody(multipartMessage);
	}

}
