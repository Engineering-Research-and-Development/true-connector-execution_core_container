package it.eng.idsa.businesslogic.processor.producer.registration;

import java.net.URISyntaxException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

@Component
public class ProducerCreatePassivateMessageProcessor extends AbstractCreateRegistrationMessage {

	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Override
	public Message getConnectorMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		return selfDescriptionService.getConnectorInactiveMessage();
	}
}
