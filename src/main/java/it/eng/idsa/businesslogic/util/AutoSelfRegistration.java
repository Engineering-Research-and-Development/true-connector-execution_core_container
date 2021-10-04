package it.eng.idsa.businesslogic.util;

import java.net.URISyntaxException;

import javax.annotation.PreDestroy;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;


@ConditionalOnProperty(name="application.selfdescription.registrateOnStartup", havingValue="true")
@Component
public class AutoSelfRegistration {
	
	private static final Logger logger = LoggerFactory.getLogger(AutoSelfRegistration.class);
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Autowired
	private BrokerService brokerService;
	
	@PreDestroy
	public void selfPassivate() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		logger.info("Starting sign out process from broker");

		brokerService.sendBrokerRequest(selfDescriptionService.getConnectorInactiveMessage(),
				selfDescriptionService.getConnectorSelfDescription());

		logger.info("Sign out process finished");
	}
}