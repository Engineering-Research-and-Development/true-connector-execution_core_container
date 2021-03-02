package it.eng.idsa.businesslogic.util;

import java.net.URISyntaxException;

import javax.annotation.PreDestroy;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.service.BrokerService;


@ConditionalOnProperty(name="application.selfdescription.registrateOnStartup", havingValue="true")
@Component
public class AutoSelfRegistration{
	
	private static final Logger logger = LogManager.getLogger(AutoSelfRegistration.class);
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Autowired
	private BrokerService brokerService;
	
	@EventListener(ApplicationReadyEvent.class)
	public void selfRegistrate() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		logger.info("Starting AutoSelfRegistration");

		brokerService.sendBrokerRequest(selfDescriptionService.getConnectorAvailbilityMessage(),
				selfDescriptionService.getConnectorSelfDescription());

		logger.info("AutoSelfRegistration finished");
	}
	
	@PreDestroy
	public void selfPassivate() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		logger.info("Starting sign out process from broker");

		brokerService.sendBrokerRequest(selfDescriptionService.getConnectorInactiveMessage(),
				selfDescriptionService.getConnectorSelfDescription());

		logger.info("Sign out process finished");

	}
	


}
