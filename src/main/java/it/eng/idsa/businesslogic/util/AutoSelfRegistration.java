package it.eng.idsa.businesslogic.util;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;


@ConditionalOnProperty(name="application.selfdescription.registrateOnStartup", havingValue="true")
@Component
public class AutoSelfRegistration{
	
	private static final Logger logger = LoggerFactory.getLogger(AutoSelfRegistration.class);
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Autowired
	private BrokerService brokerService;
	
	@EventListener(ApplicationReadyEvent.class)
	public void selfRegistrate() {
		logger.info("Starting AutoSelfRegistration");

		brokerService.sendBrokerRequest(selfDescriptionService.getConnectorAvailbilityMessage(),
				selfDescriptionService.getConnectorSelfDescription());

		logger.info("AutoSelfRegistration finished");
	}
	
	@PreDestroy
	public void selfPassivate() {
		logger.info("Starting sign out process from broker");

		brokerService.sendBrokerRequest(selfDescriptionService.getConnectorInactiveMessage(),
				selfDescriptionService.getConnectorSelfDescription());

		logger.info("Sign out process finished");

	}
	


}
