package it.eng.idsa.businesslogic.util;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;


@ConditionalOnProperty(name="application.selfdescription.registrateOnStartup", havingValue="true")
@Component
public class OnShutdown {
	
	private static final Logger logger = LoggerFactory.getLogger(OnShutdown.class);
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Autowired
	private BrokerService brokerService;
	
	@PreDestroy
	public void selfPassivate() {
		logger.info("Starting sign out process from broker");

		brokerService.sendBrokerRequest(selfDescriptionService.getConnectorInactiveMessage(),
				selfDescriptionService.getConnectorSelfDescription());

		logger.info("Sign out process finished");
	}
}