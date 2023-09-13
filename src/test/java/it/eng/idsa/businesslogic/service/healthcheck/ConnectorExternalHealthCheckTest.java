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

import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.DapsService;

public class ConnectorExternalHealthCheckTest {

	@InjectMocks
	private ConnectorExternalHealthCheck externalHealthCheck;
	@Mock
	private ClearingHouseService clearingHouseService;
	@Mock
	private DapsService dapsService;
	@Mock
	private HealthCheckConfiguration healthCheckConfiguration;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		externalHealthCheck = new ConnectorExternalHealthCheck(Optional.of(dapsService), Optional.of(clearingHouseService), 
				healthCheckConfiguration);
		when(healthCheckConfiguration.getDaps()).thenReturn("daps");
		when(healthCheckConfiguration.getClearinghouse()).thenReturn("clearinghouse");
	}
	
	@Test
	public void healthCheckTrue() {
		when(dapsService.isDapsAvailable(healthCheckConfiguration.getDaps())).thenReturn(true);
		when(clearingHouseService.isClearingHouseAvailable(healthCheckConfiguration.getClearinghouse())).thenReturn(true);

		boolean result = externalHealthCheck.checkConnectorExternalHealth();
		
		assertTrue(result);
	}
	
	@Test
	public void healthCheckFalse() {
		when(dapsService.isDapsAvailable(healthCheckConfiguration.getDaps())).thenReturn(true);
		when(clearingHouseService.isClearingHouseAvailable(healthCheckConfiguration.getClearinghouse())).thenReturn(false);

		boolean result = externalHealthCheck.checkConnectorExternalHealth();
		
		assertFalse(result);
	}
	
	@Test
	public void healthCheckFalseServicesDisabled() {
		externalHealthCheck = new ConnectorExternalHealthCheck(Optional.empty(), Optional.empty(), healthCheckConfiguration);

		boolean result = externalHealthCheck.checkConnectorExternalHealth();
		
		assertTrue(result);
	}
}
