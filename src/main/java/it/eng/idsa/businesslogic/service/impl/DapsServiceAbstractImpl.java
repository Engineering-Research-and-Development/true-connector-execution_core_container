package it.eng.idsa.businesslogic.service.impl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.annotations.VisibleForTesting;

import it.eng.idsa.businesslogic.service.DapsService;
import okhttp3.OkHttpClient;

@Service
@Transactional
public abstract class DapsServiceAbstractImpl implements DapsService {

	private static final Logger logger = LoggerFactory.getLogger(DapsServiceAbstractImpl.class);

	@Autowired
	protected OkHttpClient client;
	
	@Autowired
	protected DapsUtilityProvider dapsUtilityProvider;
	
	protected String token = null;

	@Value("${application.dapsUrl}")
	protected String dapsUrl;

	@Override
	public String getJwtToken() {

		token = getJwTokenInternal();

		if (StringUtils.isNotBlank(token) && validateToken(token)) {
			logger.info("Token is valid: " + token);
		} else {
			logger.info("Token is invalid");
			return null;
		}
		return token;
	}
	
	@Override
	public boolean validateToken(String tokenValue) {
		boolean valid = false;
		if(tokenValue==null) {
			logger.error("Token is null");
			return valid;
		}
		try {
			DecodedJWT jwt = JWT.decode(tokenValue);
			Algorithm algorithm = dapsUtilityProvider.provideAlgorithm(tokenValue);
			algorithm.verify(jwt);
			valid = true;
			if (jwt.getExpiresAt().before(new Date())) {
				valid = false;
				logger.warn("Token expired");
			}
		} catch (SignatureVerificationException e) {
			logger.info("Token did not verified, {}", e);
		} catch (JWTDecodeException e) {
			logger.error("Invalid token, {}", e);
		}
		return valid;
	}	
	
	@VisibleForTesting
	protected abstract String getJwTokenInternal();
	
}