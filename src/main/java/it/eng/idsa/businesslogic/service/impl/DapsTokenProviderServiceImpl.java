package it.eng.idsa.businesslogic.service.impl;

import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.nimbusds.jwt.JWTParser;

import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

@Service
public class DapsTokenProviderServiceImpl implements DapsTokenProviderService {

	private static final Logger logger = LogManager.getLogger(DapsTokenProviderServiceImpl.class);

	@Autowired
	private DapsService dapsService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

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
				try {
					expirationTime = JWTParser.parse(cachedToken).getJWTClaimsSet().getExpirationTime().getTime();
				} catch (ParseException e) {
					logger.error("Could not get token expiration time {}", e.getMessage());
					rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, null);
				}
			}
			return cachedToken;
		} else {
			//Always new token
			return dapsService.getJwtToken();
		}
	}
	
	
	@EventListener(ApplicationReadyEvent.class)
	@Order(1)
	public void fetchTokenOnStartup() {
		logger.info("Fetching new token on startup");
		if (fetchTokenOnStartup) {
			provideToken();
		}
	}

}
