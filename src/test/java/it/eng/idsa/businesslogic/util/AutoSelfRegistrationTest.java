package it.eng.idsa.businesslogic.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

public class AutoSelfRegistrationTest {
	
	private String CONNECTOR_SELF_DESCRIPTION = "CONNECTOR_SELF_DESCRIPTION";

	@InjectMocks
	private AutoSelfRegistration autoRegisterService;
	
	@Mock
	private SelfDescriptionService selfDescriptionService;
	
	@Mock
	private BrokerService brokerService;
	
	@Mock
	private Message idsMessage;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void register() {
		when(selfDescriptionService.getConnectorAvailbilityMessage()).thenReturn(idsMessage);
		when(selfDescriptionService.getConnectorSelfDescription()).thenReturn(CONNECTOR_SELF_DESCRIPTION);
		
		autoRegisterService.selfRegistrate();
		
		verify(brokerService).sendBrokerRequest(idsMessage, CONNECTOR_SELF_DESCRIPTION);
	}
	
	@Test
	public void passivate() {
		when(selfDescriptionService.getConnectorInactiveMessage()).thenReturn(idsMessage);
		when(selfDescriptionService.getConnectorSelfDescription()).thenReturn(CONNECTOR_SELF_DESCRIPTION);
		
		autoRegisterService.selfPassivate();
		
		verify(brokerService).sendBrokerRequest(idsMessage, CONNECTOR_SELF_DESCRIPTION);
	}
}
