package it.eng.idsa.businesslogic.util;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

@Component
public class OnShutdown {
	
	private static final Logger logger = LoggerFactory.getLogger(OnShutdown.class);
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Autowired
	private BrokerService brokerService;
	
	@Value("${application.selfdescription.registrateOnStartup}")
	String shutdownParam;
	
	@PreDestroy
	public void selfPassivate() {		
		if (shutdownParam.equals("true")) {
			logger.info("Starting sign out process from broker");

			brokerService.sendBrokerRequest(selfDescriptionService.getConnectorInactiveMessage(),
				selfDescriptionService.getConnectorSelfDescription());

			logger.info("Sign out process finished");
		}		
	}
}