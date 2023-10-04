package it.eng.idsa.businesslogic.service.healthcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;

public class ConnectorHealthCheckSchedulerTest {

	@InjectMocks
	private ConnectorHealthCheckScheduler scheduler;
	@Mock
	private ConnectorInternalHealthCheck connectorInternalCheck;
	@Mock
	private ConnectorExternalHealthCheck connectorExternalCheck;
	@Mock
	private ApplicationEventPublisher publisher;
	
	@Captor
	ArgumentCaptor<TrueConnectorEvent> tcEvent;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void checkConnectorHealth() {
		when(connectorExternalCheck.checkConnectorExternalHealth()).thenReturn(true);
		when(connectorInternalCheck.checkConnectorInternalHealth()).thenReturn(true);
		
		scheduler.checkConnectorHealth();
		
		verify(publisher, times(2)).publishEvent(tcEvent.capture());
		List<TrueConnectorEvent> tcEvents = tcEvent.getAllValues();
		assertEquals(2, tcEvents.size());
		
		assertTrue(tcEvents.stream().filter(e -> 
			TrueConnectorEventType.CONNECTOR_INTERNAL_HEALTHY.name().equals(e.getAuditEvent().getType()))
				.findFirst().isPresent());
		assertTrue(tcEvents.stream().filter(e -> 
		TrueConnectorEventType.CONNECTOR_EXTERNAL_HEALTHY.name().equals(e.getAuditEvent().getType()))
			.findFirst().isPresent());
	}
	
	@Test
	public void checkConnectorUnhealthyExternal() {
		when(connectorExternalCheck.checkConnectorExternalHealth()).thenReturn(false);
		when(connectorInternalCheck.checkConnectorInternalHealth()).thenReturn(true);
		
		scheduler.checkConnectorHealth();
		
		verify(publisher, times(2)).publishEvent(tcEvent.capture());
		List<TrueConnectorEvent> tcEvents = tcEvent.getAllValues();
		assertEquals(2, tcEvents.size());
		
		assertTrue(tcEvents.stream().filter(e -> 
			TrueConnectorEventType.CONNECTOR_INTERNAL_HEALTHY.name().equals(e.getAuditEvent().getType()))
				.findFirst().isPresent());
		assertTrue(tcEvents.stream().filter(e -> 
		TrueConnectorEventType.CONNECTOR_EXTERNAL_UNHEALTHY.name().equals(e.getAuditEvent().getType()))
			.findFirst().isPresent());
	}
	
	@Test
	public void checkConnectorUnhealthyInternal() {
		when(connectorExternalCheck.checkConnectorExternalHealth()).thenReturn(true);
		when(connectorInternalCheck.checkConnectorInternalHealth()).thenReturn(false);
		
		scheduler.checkConnectorHealth();
		
		verify(publisher, times(2)).publishEvent(tcEvent.capture());
		List<TrueConnectorEvent> tcEvents = tcEvent.getAllValues();
		assertEquals(2, tcEvents.size());
		
		assertTrue(tcEvents.stream().filter(e -> 
			TrueConnectorEventType.CONNECTOR_INTERNAL_UNHEALTHY.name().equals(e.getAuditEvent().getType()))
				.findFirst().isPresent());
		assertTrue(tcEvents.stream().filter(e -> 
		TrueConnectorEventType.CONNECTOR_EXTERNAL_HEALTHY.name().equals(e.getAuditEvent().getType()))
			.findFirst().isPresent());
	}
}
