package it.eng.idsa.businesslogic.service.healthcheck;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;

/**
 * Logic used to check connector internal health, like connection to DataApp, Usage Control, 
 * Audit logs volume space,...
 * @author igor.balog
 *
 */
@Service
public class ConnectorInternalHealthCheck {
	
	private static final Logger logger = LoggerFactory.getLogger(ConnectorInternalHealthCheck.class);
	
	private String dataAppHealthURL;
	
	private CommunicationService communicationService;
	private Optional<UsageControlService> usageControlService;
	private AuditLogHealthCheck auditLogHealthService;
	
	public ConnectorInternalHealthCheck(
			@Value("${application.openDataAppReceiverHealth}") String dataAppHealthURL,
			CommunicationService communicationService,
			Optional<UsageControlService> usageControlService,
			AuditLogHealthCheck auditLogHealthService) {
		this.dataAppHealthURL = dataAppHealthURL;
		this.communicationService = communicationService;
		this.usageControlService = usageControlService;
		this.auditLogHealthService = auditLogHealthService;
	}

	public boolean checkConnectorInternalHealth() {
		logger.debug("Checking connector INTERNAL services");
		boolean auditDiskSpace = auditLogHealthService.isAuditLogVolumeHealthy();
		boolean dataAppAvailabiltity = checkDataAppAvailability();
		boolean usageControlAvailability = checkUsageControlAvailability();
		logger.info("Internal health check:\nAudit disk space - {}\nDataAppAvailabile - {}\nUsageControlAvailable - {}",
				auditDiskSpace, dataAppAvailabiltity, usageControlAvailability);
		boolean internalHealth = auditDiskSpace && dataAppAvailabiltity && usageControlAvailability;
		logger.info("Connector INTERNAL health check is {}", internalHealth ? "HEALTHY" : "UNHEALTHY");
		return internalHealth;
	}
	
	private boolean checkDataAppAvailability() {
		String response = communicationService.getRequest(dataAppHealthURL);
		return response != null;
	}
	
	private boolean checkUsageControlAvailability() {
		return usageControlService.map(UsageControlService::isUsageControlAvailable)
                .orElse(true);
	}
	
}