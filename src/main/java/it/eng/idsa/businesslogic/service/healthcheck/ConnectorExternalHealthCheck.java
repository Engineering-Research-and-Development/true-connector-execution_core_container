package it.eng.idsa.businesslogic.service.healthcheck;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.DapsService;

/**
 * Logic used to check connector external health, like connection to DAPS,
 * Clearing House, Broker if needed,...
 * 
 * @author igor.balog
 *
 */
@Service
public class ConnectorExternalHealthCheck {
	private static final Logger logger = LoggerFactory.getLogger(ConnectorInternalHealthCheck.class);

	private Optional<ClearingHouseService> clearingHouseService;
	private Optional<DapsService> dapsService;

	public ConnectorExternalHealthCheck(Optional<DapsService> dapsService, 
			Optional<ClearingHouseService> clearingHouseService) {
		super();
		this.dapsService = dapsService;
		this.clearingHouseService = clearingHouseService;
	}

	public boolean checkConnectorExternalHealth() {
		logger.debug("Checking if EXTERNAL services are available");
		boolean dapsAvailable = checkDAPSAvailability();
		boolean chAvailable = checkClearingHouseAvailability();
		logger.info("External health check:\nDAPSAvailabile - {}\nClearingHouseAvailable - {}",
				dapsAvailable, chAvailable);
		boolean externalHealth = dapsAvailable  && chAvailable;
		logger.info("Connector EXTERNAL health check is {}", externalHealth ? "HEALTHY" : "UNHEALTHY");
		return externalHealth;
	}

	private boolean checkDAPSAvailability() {
		return dapsService.map(DapsService::isDapsAvailable).orElse(true);
	}

	private boolean checkClearingHouseAvailability() {
		return clearingHouseService.map(ClearingHouseService::isClearingHouseAvailable).orElse(true);
	}
}
