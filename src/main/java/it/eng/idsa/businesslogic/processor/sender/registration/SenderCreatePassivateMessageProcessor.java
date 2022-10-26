package it.eng.idsa.businesslogic.processor.sender.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class SenderCreatePassivateMessageProcessor extends AbstractCreateRegistrationMessage {

	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Override
	public Message getConnectorMessage() {
		return selfDescriptionService.getConnectorInactiveMessage();
	}

	@Override
	void publishEvent(MultipartMessage multipartMessage, String correlationId) {
		publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE, 
				multipartMessage, correlationId));
	}
}
