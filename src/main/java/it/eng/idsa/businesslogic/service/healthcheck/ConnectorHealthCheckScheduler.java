package it.eng.idsa.businesslogic.service.healthcheck;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ConnectorHealthCheckScheduler {
	
	private static final Logger logger = LoggerFactory.getLogger(ConnectorHealthCheckScheduler.class);
	
	private ConnectorInternalHealthCheck connectorInternalCheck;
	private ConnectorExternalHealthCheck connectorExternalCheck;

	public ConnectorHealthCheckScheduler(ConnectorInternalHealthCheck connectorInternalCheck,
			ConnectorExternalHealthCheck connectorExternalCheck) {
		this.connectorInternalCheck = connectorInternalCheck;
		this.connectorExternalCheck = connectorExternalCheck;
	}
	
	@Scheduled(cron = "${cron.expression}")
	public void connectorHealthCheck() {
		logger.info("Updating connector health status");
		connectorInternalCheck.checkConnectorInternalHealth();
	}
	
	@PostConstruct
	public void checkConnectorInternalHealth() {
		logger.info("Checking connector internal and external health!");
		boolean connectorHealth = connectorInternalCheck.checkConnectorInternalHealth() && 
				connectorExternalCheck.checkConnectorExternalHealth();
		logger.info("Connector is in {} state", connectorHealth ? "HEALTHY" : "UNHEALTHY");
		ConnectorHealthCheck.getInstance().setConnectorHealth(connectorHealth);
	}
}
