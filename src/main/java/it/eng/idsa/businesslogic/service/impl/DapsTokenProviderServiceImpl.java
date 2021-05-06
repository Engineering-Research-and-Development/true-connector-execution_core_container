package it.eng.idsa.businesslogic.service.impl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.multipart.util.TestUtilMessageService;

@Service
public class DapsTokenProviderServiceImpl implements DapsTokenProviderService {

	private static final Logger logger = LoggerFactory.getLogger(DapsTokenProviderServiceImpl.class);

	@Autowired
	private DapsService dapsService;

	private String cachedToken;

	private Date expirationTime;

	@Value("${application.tokenCaching}")
	private boolean tokenCaching;
	
	@Value("${application.fetchTokenOnStartup}")
	private boolean fetchTokenOnStartup;
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean useDaps;
	
	@Override
	public String provideToken() {
		logger.info("Requesting token");
		if(!useDaps) {
			logger.info("Daps not configured but must use some token - using value {}", TestUtilMessageService.TOKEN_VALUE);
			return TestUtilMessageService.TOKEN_VALUE;
		}
		if (tokenCaching) {
			//Checking if cached token is still valid
			if (cachedToken == null || new Date().after(expirationTime)) {
				logger.info("Fetching new token");
				cachedToken = dapsService.getJwtToken();
				if (cachedToken != null) {
					try {
						expirationTime = JWT.decode(cachedToken).getExpiresAt();
					} catch (JWTDecodeException e) {
						logger.error("Could not get token expiration time {}", e.getMessage());
						//Setting to default values since the JWT token was not correct
						cachedToken = null;
						expirationTime = null;
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

	@Override
	public DynamicAttributeToken getDynamicAtributeToken() {
		return new DynamicAttributeTokenBuilder()
				._tokenFormat_(TokenFormat.JWT)
				._tokenValue_(this.provideToken())
				.build();	
	}
}
