package it.eng.idsa.businesslogic.service.impl;

import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.nimbusds.jwt.JWTParser;

import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;

@Service
public class DapsTokenProviderServiceImpl implements DapsTokenProviderService {

	private static final Logger logger = LogManager.getLogger(DapsTokenProviderServiceImpl.class);

	@Autowired
	private DapsService dapsService;

	private String cachedToken;

	private long expirationTime;

	@Value("${application.tokenCaching}")
	private boolean tokenCaching;
	
	@Value("${application.fetchTokenOnStartup}")
	private boolean fetchTokenOnStartup;

	@Override
	public String provideToken() {
		logger.info("Requesting token");
		if (tokenCaching) {
			//Checking if cached token is still valid
			if (cachedToken == null || System.currentTimeMillis() > expirationTime) {
				logger.info("Fetching new token");
				cachedToken = dapsService.getJwtToken();
				if (cachedToken != null) {
					try {
						expirationTime = JWTParser.parse(cachedToken).getJWTClaimsSet().getExpirationTime().getTime();
					} catch (ParseException e) {
						logger.error("Could not get token expiration time {}", e.getMessage());
						//Setting to default values since the JWT token was not correct
						cachedToken = null;
						expirationTime = 0;
					} 
				} 
			}
			return cachedToken;
		} else {
			//Always new token
			return dapsService.getJwtToken();
		}
	}
	
	
	@EventListener(ApplicationReadyEvent.class)
	public void fetchTokenOnStartup() {
		if (fetchTokenOnStartup && StringUtils.isBlank(cachedToken)) {
			logger.info("Fetching DAT token on startup");
			provideToken();
		}
	}

}
