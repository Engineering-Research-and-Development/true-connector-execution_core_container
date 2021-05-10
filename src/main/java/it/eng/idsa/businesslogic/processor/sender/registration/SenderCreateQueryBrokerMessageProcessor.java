package it.eng.idsa.businesslogic.processor.sender.registration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.service.impl.ProtocolValidationServiceImpl;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class SenderCreateQueryBrokerMessageProcessor implements Processor {
	
	@Autowired
	private ProtocolValidationServiceImpl protocolValidationServiceImpl;

	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String forwardTo = exchange.getMessage().getHeader("Forward-To").toString();
		forwardTo = protocolValidationServiceImpl.validateProtocol(forwardTo);
		exchange.getMessage().setHeader("Forward-To", forwardTo);
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(selfDescriptionService.getConnectorQueryMessage())
				.withPayloadContent(exchange.getMessage().getBody(String.class)).build();

		exchange.getMessage().setBody(multipartMessage);
	}

}
