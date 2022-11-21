package it.eng.idsa.businesslogic.service.healthcheck;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuditLogHealthCheckTest {

	public AuditLogHealthCheck auditLogCheck;
	
	@BeforeEach
	public void setup() {
		auditLogCheck = new AuditLogHealthCheck();
	}
	
	@Test
	public void checkAuditVolume() {
		boolean isValid = auditLogCheck.isAuditLogVolumeHealthy();
		assertTrue(isValid);
	}
}
