package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */

@ConditionalOnProperty(name = "application.dapsVersion", havingValue = "v2")
@Service
@Transactional
public class DapsV2ServiceImpl extends DapsServiceAbstractImpl {
	private static final Logger logger = LoggerFactory.getLogger(DapsV2ServiceImpl.class);

	@VisibleForTesting
	@Override
	public String getJwTokenInternal() {

		Response jwtResponse = null;
		try {
			logger.info("Retrieving Dynamic Attribute Token...");

			String jws = dapsUtilityProvider.getDapsV2Jws();
			logger.info("Request token: " + jws);

			// build form body to embed client assertion into post request
			RequestBody formBody = new FormBody.Builder()
					.add("grant_type", "client_credentials")
					.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
					.add("client_assertion", jws)
					.add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
					.build();

			Request request = new Request.Builder()
					.url(dapsUrl)
					.post(formBody)
					.build();
			jwtResponse = client.newCall(request).execute();
			if (!jwtResponse.isSuccessful()) {
				throw new IOException("Unexpected code " + jwtResponse);
			}
			var responseBody = jwtResponse.body();
			if (responseBody == null) {
				throw new Exception("JWT response is null.");
			}
			var jwtString = responseBody.string();
			logger.info("Response body of token request:\n{}", jwtString);
			ObjectNode node = new ObjectMapper().readValue(jwtString, ObjectNode.class);

			if (node.has("access_token")) {
				token = node.get("access_token").asText();
				logger.debug("access_token: {}", token.toString());
			} else {
				logger.info("jwtResponse: {}", jwtResponse.toString());
			}
		} catch (KeyStoreException 
				| NoSuchAlgorithmException 
				| CertificateException 
				| UnrecoverableKeyException e) {
			logger.error("Cannot acquire token:", e);
		} catch (IOException e) {
			logger.error("Error retrieving token:", e);
		} catch (Exception e) {
			logger.error("Something else went wrong:", e);
		} finally {
			if (jwtResponse != null) {
				jwtResponse.close();
			}
		}
		return token;
	}	
}