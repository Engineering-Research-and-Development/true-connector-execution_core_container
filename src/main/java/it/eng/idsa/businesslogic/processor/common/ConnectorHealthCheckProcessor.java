package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.healthcheck.ConnectorHealthCheck;

@Component
public class ConnectorHealthCheckProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ConnectorHealthCheckProcessor.class);
	
	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("Performing health check!");
		boolean connectorHealthy = ConnectorHealthCheck.getInstance().getConnectorHealth();
		logger.info("Connector health: {}", connectorHealthy);
		if(!connectorHealthy) {
			logger.info("Connector not in healthy state - please check logs for more details");
			rejectionMessageService.sendRejectionMessage(null, RejectionReason.TEMPORARILY_NOT_AVAILABLE);
		}
	}

}
