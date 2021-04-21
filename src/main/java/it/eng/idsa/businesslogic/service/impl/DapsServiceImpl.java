package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.idsa.businesslogic.service.DapsService;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

/**
 * Service Implementation for managing DAPS.
 */
@ConditionalOnProperty(name = "application.dapsVersion", havingValue = "v1")
@Service
@Transactional
public class DapsServiceImpl implements DapsService {

	private static final Logger logger = LoggerFactory.getLogger(DapsServiceImpl.class);

	private String token = null;
	
	@Autowired
	private DapsUtilityProvider dapsUtilityProvider;
	
	@Autowired
	private OkHttpClient client;

	@Value("${application.dapsUrl}")
	private String dapsUrl;
  
	private String getJwTokenInternal() {

		logger.debug("Get properties");

		Response responseDaps = null;
		try {
			logger.debug("Started get JWT token");
			
			String jws = dapsUtilityProvider.getDapsV1Jws();
			logger.info("Request token: " + jws);
			
			// @formatter:off
			RequestBody formBody = new FormBody.Builder()
					.add("grant_type", "client_credentials")
					.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
					.add("client_assertion", jws)
					.build();

			Request requestDaps = new Request.Builder()
					.url(dapsUrl)
					.post(formBody)
					.build();
			// @formatter:on
			responseDaps = client.newCall(requestDaps).execute();

			if (!responseDaps.isSuccessful())
				throw new IOException("Unexpected code " + responseDaps);

			String body = responseDaps.body().string();
			ObjectNode node = new ObjectMapper().readValue(body, ObjectNode.class);
            if (node.has("access_token")) {
                token = node.get("access_token").asText();
                logger.info("access_token: {}", token);
            }

		} catch (IOException  e) {
			logger.error("Error while making a request to fetch token", e);
			return null;
		} finally {
			if (responseDaps != null) {
				responseDaps.close();
			}
		}
		return token;
	}

	
	@Override
	public boolean validateToken(String tokenValue) {
		boolean valid = false;
		DecodedJWT jwt = JWT.decode(tokenValue);
		try {
			Algorithm algorithm = dapsUtilityProvider.provideAlgorithm(tokenValue);
			algorithm.verify(jwt);
			valid = true;
			if (jwt.getExpiresAt().before(new Date())) {
				valid = false;
				logger.warn("Token expired");
			}
		} catch (SignatureVerificationException e) {
			logger.info("Token did not verified, {}", e);
		}
		return valid;
	}


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
}
