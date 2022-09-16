package it.eng.idsa.businesslogic.processor.sender.registration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SenderCreateUpdateMessageProcessorTest {

	@InjectMocks
	private SenderCreateUpdateMessageProcessor processor;
	
	@Mock
	private SelfDescriptionService selfDescriptionService;
	
	ConnectorUpdateMessage connectorAvailable = UtilMessageService.getConnectorUpdateMessage(
			UtilMessageService.SENDER_AGENT, 
			UtilMessageService.ISSUER_CONNECTOR, 
			UtilMessageService.AFFECTED_CONNECTOR);

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void getConnectorMessage() {
		when(selfDescriptionService.getConnectorUpdateMessage()).thenReturn(connectorAvailable);

		var message = processor.getConnectorMessage();
		
		assertTrue(message instanceof ConnectorUpdateMessage);
	}
}
