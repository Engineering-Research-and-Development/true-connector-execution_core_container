package it.eng.idsa.businesslogic.processor.sender.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

@Component
public class SenderCreateRegistrationMessageProcessor extends AbstractCreateRegistrationMessage {

	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Autowired
	private ApplicationEventPublisher publisher;

	@Override
	public Message getConnectorMessage() {
		publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER, null));
		return selfDescriptionService.getConnectorAvailbilityMessage();
	}
}
