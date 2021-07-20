package it.eng.idsa.businesslogic.processor.sender.registration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class SenderCreateQueryBrokerMessageProcessor implements Processor {
	
	@Autowired
	private ProtocolValidationService protocolValidationService;

	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Override
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
		
		
		String forwardTo = exchange.getMessage().getHeader("Forward-To").toString();
		forwardTo = protocolValidationService.validateProtocol(forwardTo, multipartMessage.getHeaderContent());

		exchange.getMessage().setHeader("Forward-To", forwardTo);
		exchange.getMessage().setBody(multipartMessage);
	}

}
