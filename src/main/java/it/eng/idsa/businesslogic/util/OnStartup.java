package it.eng.idsa.businesslogic.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.service.impl.DapsTokenProviderServiceImpl;

public class OnStartup {

	private static final Logger logger = LoggerFactory.getLogger(OnStartup.class);
	
	@Autowired
	private DapsTokenProviderServiceImpl dapsTokenProviderServiceImpl;
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Autowired
	private BrokerService brokerService;
	
	private String cachedToken;
	
	@Value("${application.fetchTokenOnStartup}")
	private boolean fetchTokenOnStartup;
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean useDaps;
		
	@EventListener(ApplicationReadyEvent.class)
	public void fetchTokenOnStartup() {
		if ((fetchTokenOnStartup && useDaps) && StringUtils.isBlank(cachedToken)) {
			logger.info("Fetching DAT token on startup");
			dapsTokenProviderServiceImpl.provideToken();
		}
	}
	
	@Bean(name = "autoSelfRegistration")
	@ConditionalOnProperty(name="application.selfdescription.registrateOnStartup", havingValue="true")
	@EventListener(ApplicationReadyEvent.class)
	public void selfRegistrate() {
		logger.info("Starting AutoSelfRegistration");

		brokerService.sendBrokerRequest(selfDescriptionService.getConnectorAvailbilityMessage(),
				selfDescriptionService.getConnectorSelfDescription());

		logger.info("AutoSelfRegistration finished");
	}	
}