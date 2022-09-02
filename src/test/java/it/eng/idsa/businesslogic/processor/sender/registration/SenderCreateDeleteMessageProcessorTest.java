package it.eng.idsa.businesslogic.processor.sender.registration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SenderCreateDeleteMessageProcessorTest {

	@InjectMocks
	private SenderCreateDeleteMessageProcessor processor;
	
	@Mock
	private SelfDescriptionService selfDescriptionService;
	
	ConnectorUnavailableMessage connectorAvailable = UtilMessageService.getConnectorUnavailableMessage(
			UtilMessageService.SENDER_AGENT, 
			UtilMessageService.ISSUER_CONNECTOR, 
			UtilMessageService.AFFECTED_CONNECTOR);
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void getConnectorMessage() {
		when(selfDescriptionService.getConnectorUnavailableMessage()).thenReturn(connectorAvailable);

		var message = processor.getConnectorMessage();
		
		assertTrue(message instanceof ConnectorUnavailableMessage);
	}
}
