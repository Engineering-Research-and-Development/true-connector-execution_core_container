package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.eng.idsa.businesslogic.service.DapsService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

/**
 * Service Implementation for managing DAPS.
 */
//@ConditionalOnProperty(name = "application.dapsVersion", havingValue = "orbiter")
@ConditionalOnExpression("'${application.isEnabledDapsInteraction}' == 'true' && '${application.dapsVersion}'=='orbiter'")
@Service
@Transactional
public class DapsOrbiterServiceImpl implements DapsService {

	private static final Logger logger = LoggerFactory.getLogger(DapsOrbiterServiceImpl.class);
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private String token = null;

	@Value("${application.dapsUrl}")
	private String dapsUrl;
	@Value("${application.connectorUUID}")
	private String connectorUUID;
	@Value("${application.daps.orbiter.password}")
	private String dapsOrbiterPassword;
	@Value("${application.dapsJWKSUrl}")
	private URL dapsJWKSUrl;

	@Autowired
	private OkHttpClient client;

	@Autowired
	private DapsOrbiterProvider dapsOrbiterProvider;

	@VisibleForTesting
	String getJwTokenInternal() {
		// Try clause for setup phase (loading keys, building trust manager)
		Response jwtResponse = null;
		try {
			logger.info("Retrieving Dynamic Attribute Token...");

			String jws = dapsOrbiterProvider.provideJWS();

			// build form body to embed client assertion into post request
			Map<String, String> jsonObject = new HashMap<>();
			jsonObject.put("grant_type", "client_credentials");
			jsonObject.put("client_assertion_type", "jwt-bearer");
			jsonObject.put("client_assertion", jws);
			jsonObject.put("scope", "all");
			Gson gson = new GsonBuilder().create();
			String jsonString = gson.toJson(jsonObject);
			RequestBody formBody = RequestBody.create(jsonString, JSON); // new

			// @formatter:off
			Request requestDaps = new Request.Builder()
					.url(dapsUrl)
					.header("Host", "ecc-receiver")
					.header("accept", "application/json")
					.header("Content-Type", "application/json")
					.post(formBody)
					.build();
			// @formatter:on
			
			jwtResponse = client.newCall(requestDaps).execute();
			if (!jwtResponse.isSuccessful()) {
				throw new IOException("Unexpected code " + jwtResponse);
			}
			var responseBody = jwtResponse.body();
			if (responseBody == null) {
				throw new Exception("JWT response is null.");
			}
			var jwtString = responseBody.string();
			logger.info("Received response from DAPS");
			ObjectNode node = new ObjectMapper().readValue(jwtString, ObjectNode.class);

			 if (node.has("response")) {
	                token = node.get("response").asText();
	            }
	        } catch (KeyStoreException
	                | NoSuchAlgorithmException
	                | CertificateException
	                | UnrecoverableKeyException e) {
	            logger.error("Cannot acquire token:", e);
	        } catch (JsonParseException e) {
	            logger.error("JSON not received as response", e);
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
	/**
	 * Send request towards Orbiter to validate if token is correct
	 */
	public boolean validateToken(String tokenValue) {
		boolean isValid = false;
		if(tokenValue==null) {
			logger.error("Token is null");
			return isValid;
		}

		logger.debug("Validating Orbiter token");

		if(checkIfTokenExpired(tokenValue)) {
			return false;
		}
		
		Response jwtResponse = null;
		try {
			Map<String, String> jsonObject = new HashMap<>();
			jsonObject.put("token", tokenValue);
			Gson gson = new GsonBuilder().create();
			String jsonString = gson.toJson(jsonObject);
			RequestBody formBody = RequestBody.create(jsonString, JSON); // new

			// @formatter:off
			Request requestDaps = new Request.Builder()
					.url(dapsUrl + "/validate")
					.header("Host", "ecc-receiver")
					.header("accept", "application/json")
					.header("Content-Type", "application/json")
					.post(formBody)
					.build();
			// @formatter:on

			jwtResponse = client.newCall(requestDaps).execute();

			ResponseBody responseBody = jwtResponse.body();
			String response = responseBody.string();
			if (!jwtResponse.isSuccessful()) {
				throw new IOException("Error when validating token: " + jwtResponse);
			}

			logger.info("Received response from DAPS");
			// parse body and check if content is like following
//			{
//			    "response": true,
//			    "description": "Token successfully validated"
//			}
//			otherwise we will get 'invalid token'
			try {
				ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);
				if (node.has("response") && node.get("response").asBoolean()) {
					logger.info("Token successfuly validated - signature OK");
					isValid = true;
				}
			} catch (JsonProcessingException ex) {
				logger.info("Token was not validated correctly: {}", ex);
			}
		} catch (Exception e) {
			logger.error("Error while validating token", e);
		} finally {
			if (jwtResponse != null) {
				jwtResponse.close();
			}
		}
		return isValid;
	}

	private boolean checkIfTokenExpired(String tokenValue) {
		DecodedJWT jwt = null;
		try {
			jwt = JWT.decode(tokenValue);
		} catch (JWTDecodeException e) {
			logger.error("Invalid token, {}", e);
		}
		if (jwt.getExpiresAt().before(new Date())) {
			logger.warn("Token expired");
			return true;
		}
		return false;
	}

	@Override
	public String getJwtToken() {

		token = getJwTokenInternal();

		if (StringUtils.isNotBlank(token) && validateToken(token)) {
			logger.info("Token is valid");
		} else {
			logger.info("Token is invalid");
			return null;
		}
		return token;
	}

	@Override
	public boolean isDapsAvailable(String dapsHealthCheckEndpoint) {
		Request request = new Request.Builder().url(dapsHealthCheckEndpoint).build();
		try {
			Response response =  client.newCall(request).execute();
			if(response.isSuccessful()) {
				return true;
			}
		} catch (IOException e) {
			logger.error("Error while making call to {}", dapsUrl, e);
			return false;
		}
		return false;
	}

	@Override
	public String getConnectorUUID() {
		return null;
	}
}
