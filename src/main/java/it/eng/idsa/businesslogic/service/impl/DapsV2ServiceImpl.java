package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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
import com.google.common.annotations.VisibleForTesting;

import it.eng.idsa.businesslogic.service.DapsService;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */

@ConditionalOnProperty(name = "application.dapsVersion", havingValue = "v2")
@Service
@Transactional
public class DapsV2ServiceImpl implements DapsService {
	private static final Logger logger = LoggerFactory.getLogger(DapsV2ServiceImpl.class);

	@Autowired
	private OkHttpClient client;
	
	@Autowired
	private DapsUtilityProvider dapsUtilityProvider;

	private String token = null;

	@Value("${application.dapsUrl}")
	private String dapsUrl;

	@VisibleForTesting
	String getJwTokenInternal() {

		Response jwtResponse = null;
		try {
			logger.info("Retrieving Dynamic Attribute Token...");

			String jws = dapsUtilityProvider.getJws();
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