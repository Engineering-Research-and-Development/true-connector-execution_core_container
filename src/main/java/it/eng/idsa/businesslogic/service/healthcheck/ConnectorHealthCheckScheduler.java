package it.eng.idsa.businesslogic.service.healthcheck;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;

@Service
@ConditionalOnProperty(name="application.healthcheck.enabled", havingValue="true")
public class ConnectorHealthCheckScheduler {
	
	private static final Logger logger = LoggerFactory.getLogger(ConnectorHealthCheckScheduler.class);
	
	private ConnectorInternalHealthCheck connectorInternalCheck;
	private ConnectorExternalHealthCheck connectorExternalCheck;
	private ApplicationEventPublisher publisher;

	public ConnectorHealthCheckScheduler(ConnectorInternalHealthCheck connectorInternalCheck,
			ConnectorExternalHealthCheck connectorExternalCheck,
			ApplicationEventPublisher publisher) {
		this.connectorInternalCheck = connectorInternalCheck;
		this.connectorExternalCheck = connectorExternalCheck;
		this.publisher = publisher;
	}
	
	@PostConstruct
	@Scheduled(cron = "${application.healthcheck.cron.expression}")
	public void checkConnectorHealth() {
		logger.info("Checking connector internal and external health!");
		boolean internalHealthCheck = connectorInternalCheck.checkConnectorInternalHealth();
		if(internalHealthCheck) {
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_INTERNAL_HEALTHY, null));
		} else {
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_INTERNAL_UNHEALTHY, null));
		}
		boolean externalHealthCheck = connectorExternalCheck.checkConnectorExternalHealth();
		if(externalHealthCheck) {
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_EXTERNAL_HEALTHY, null));
		} else {
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_EXTERNAL_UNHEALTHY, null));
		}
		boolean connectorHealth = internalHealthCheck && externalHealthCheck;
		logger.info("Internal health check - {}\tExternal health check - {}", internalHealthCheck, externalHealthCheck);
 		logger.info("Connector is in {} state", connectorHealth ? "HEALTHY" : "UNHEALTHY");
		ConnectorHealthCheck.getInstance().setConnectorHealth(connectorHealth);
	}
}
