package it.eng.idsa.businesslogic.service.healthcheck;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AuditLogHealthCheckTest {

	public AuditLogHealthCheck auditLogCheck;
	@Mock
	private HealthCheckConfiguration healthCheckConfiguration;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		auditLogCheck = new AuditLogHealthCheck(healthCheckConfiguration);
	}
	
	@Test
	public void checkAuditVolume() {
		boolean isValid = auditLogCheck.isAuditLogVolumeHealthy();
		assertTrue(isValid);
	}
}
