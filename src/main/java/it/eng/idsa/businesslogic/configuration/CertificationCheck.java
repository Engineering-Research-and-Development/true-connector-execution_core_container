package it.eng.idsa.businesslogic.configuration;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.Application;

@Configuration
public class CertificationCheck {

	private static final Logger logger = LoggerFactory.getLogger(CertificationCheck.class);

	private static final String[] CERTIFIED_VERSION = { "1.14.3" };
	@PostConstruct
	public void checkIfVerionsIsCertified() {
		String version = Objects.requireNonNullElse(Application.class.getPackage().getImplementationVersion(), "");
		logger.info("Certified version: " + 
				 Arrays.stream(CERTIFIED_VERSION).anyMatch(version::equals));
	}
}
