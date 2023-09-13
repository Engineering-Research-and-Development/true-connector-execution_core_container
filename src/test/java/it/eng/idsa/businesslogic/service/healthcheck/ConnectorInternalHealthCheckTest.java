package it.eng.idsa.businesslogic.service.healthcheck;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;

public class ConnectorInternalHealthCheckTest {

	@InjectMocks
	private ConnectorInternalHealthCheck iternalHealthCheck;
	
	@Mock
	private CommunicationService communicationService;
	@Mock
	private UsageControlService usageControlService;
	@Mock
	private AuditLogHealthCheck auditLogHealthService;
	@Mock
	private HealthCheckConfiguration healthCheckConfiguration;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		iternalHealthCheck = new ConnectorInternalHealthCheck(communicationService, 
				Optional.of(usageControlService), auditLogHealthService, healthCheckConfiguration);
	}
	
	@Test
	public void internalHealthTrue() {
		when(auditLogHealthService.isAuditLogVolumeHealthy()).thenReturn(true);
		when(usageControlService.isUsageControlAvailable(healthCheckConfiguration.getUsagecontrol())).thenReturn(true);
		when(communicationService.getRequest(healthCheckConfiguration.getDataapp())).thenReturn("dataapp");
		
		boolean result = iternalHealthCheck.checkConnectorInternalHealth();
		
		assertTrue(result);
	}
	
	@Test
	public void internalHealthTrueUCDisabled() {
		iternalHealthCheck = new ConnectorInternalHealthCheck(communicationService, 
				Optional.empty(), auditLogHealthService, healthCheckConfiguration);
		
		when(auditLogHealthService.isAuditLogVolumeHealthy()).thenReturn(true);
		when(communicationService.getRequest(healthCheckConfiguration.getDataapp())).thenReturn("ABC");
		
		boolean result = iternalHealthCheck.checkConnectorInternalHealth();
		
		assertTrue(result);
	}
	
	@Test
	public void internalHealthFalseDataApp() {
		when(auditLogHealthService.isAuditLogVolumeHealthy()).thenReturn(true);
		when(usageControlService.isUsageControlAvailable(healthCheckConfiguration.getUsagecontrol())).thenReturn(true);
		when(communicationService.getRequest(healthCheckConfiguration.getDataapp())).thenReturn(null);
		
		boolean result = iternalHealthCheck.checkConnectorInternalHealth();
		
		assertFalse(result);
	}
	
	@Test
	public void internalHealthFalseUC() {
		when(auditLogHealthService.isAuditLogVolumeHealthy()).thenReturn(true);
		when(usageControlService.isUsageControlAvailable(healthCheckConfiguration.getUsagecontrol())).thenReturn(false);
		when(communicationService.getRequest(healthCheckConfiguration.getDataapp())).thenReturn("ABC");
		
		boolean result = iternalHealthCheck.checkConnectorInternalHealth();
		
		assertFalse(result);
	}
	
	@Test
	public void internalHealthFalseAudit() {
		when(auditLogHealthService.isAuditLogVolumeHealthy()).thenReturn(false);
		when(usageControlService.isUsageControlAvailable(healthCheckConfiguration.getUsagecontrol())).thenReturn(true);
		when(communicationService.getRequest(healthCheckConfiguration.getDataapp())).thenReturn("ABC");
		
		boolean result = iternalHealthCheck.checkConnectorInternalHealth();
		
		assertFalse(result);
	}
	
	@Test
	public void internalHealthFalseAll() {
		when(auditLogHealthService.isAuditLogVolumeHealthy()).thenReturn(false);
		when(usageControlService.isUsageControlAvailable(healthCheckConfiguration.getUsagecontrol())).thenReturn(false);
		when(communicationService.getRequest(healthCheckConfiguration.getDataapp())).thenReturn(null);
		
		boolean result = iternalHealthCheck.checkConnectorInternalHealth();
		
		assertFalse(result);
	}
	
}
