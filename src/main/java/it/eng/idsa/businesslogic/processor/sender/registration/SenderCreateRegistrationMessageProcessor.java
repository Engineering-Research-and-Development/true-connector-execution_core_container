package it.eng.idsa.businesslogic.processor.sender.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

@Component
public class SenderCreateRegistrationMessageProcessor extends AbstractCreateRegistrationMessage {

	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Override
	public Message getConnectorMessage() {
		return selfDescriptionService.getConnectorAvailbilityMessage();
	}
}
