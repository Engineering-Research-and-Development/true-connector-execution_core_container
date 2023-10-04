package it.eng.idsa.businesslogic.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ShutdownConnector {
	
	private static final Logger logger = LoggerFactory.getLogger(ShutdownConnector.class);

	private ApplicationContext context;

	public ShutdownConnector(ApplicationContext context) {
		this.context = context;
	}

	public void shutdownConnector() {
		logger.error("********  SHUTTING DOWN THE CONNECTOR   ********");
		SpringApplication.exit(context);
		System.exit(-1);
	}
}
