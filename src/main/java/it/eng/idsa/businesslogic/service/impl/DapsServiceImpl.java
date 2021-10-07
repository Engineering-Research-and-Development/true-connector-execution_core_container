package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.FormBody;
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
public class DapsServiceImpl extends DapsServiceAbstractImpl {

	private static final Logger logger = LoggerFactory.getLogger(DapsServiceImpl.class);
	
	@Override
	public String getJwTokenInternal() {

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
}