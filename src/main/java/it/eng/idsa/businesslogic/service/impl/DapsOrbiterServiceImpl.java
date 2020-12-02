package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
@ConditionalOnProperty(name = "application.dapsVersion", havingValue = "orbiter")
@Service
@Transactional
public class DapsOrbiterServiceImpl implements DapsService {

    private static final Logger logger = LogManager.getLogger(DapsOrbiterServiceImpl.class);
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private String token = "";

    @Value("${application.dapsUrl}")
    private String dapsUrl;
    @Value("${application.connectorUUID}")
    private String connectorUUID;
    @Value("${application.daps.orbiter.password}")
    private String dapsOrbiterPassword;
    
    @Autowired
    private OkHttpClient client;
    
    @Autowired
    private DapsOrbiterProvider dapsOrbiterProvider;

    @Override
    public String getJwtToken() {
        // Try clause for setup phase (loading keys, building trust manager)
        try {
            logger.info("ConnectorUUID: " + connectorUUID);
            logger.info("Retrieving Dynamic Attribute Token...");
           
            String jws = dapsOrbiterProvider.provideJWS();
            logger.info("Request token: " + jws);

            // build form body to embed client assertion into post request
            Map<String, String> jsonObject = new HashMap<>();
            jsonObject.put("grant_type", "client_credentials");
            jsonObject.put("client_assertion_type", "jwt-bearer");
            jsonObject.put("client_assertion", jws);
            jsonObject.put("scope", "all");
            Gson gson = new GsonBuilder().create();
            String jsonString = gson.toJson(jsonObject);
            RequestBody formBody = RequestBody.create(JSON, jsonString); // new

            Request requestDaps = new Request.Builder().url(dapsUrl)
                    .header("Host", "ecc-receiver")
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .post(formBody).build();

            Response jwtResponse = client.newCall(requestDaps).execute();
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

            if (node.has("response")) {
                token = node.get("response").asText();
                logger.info("access_token: {}", token.toString());
            }
            logger.info("access_token: {}", jwtResponse.toString());
            logger.info("access_token: {}", jwtString);
            logger.info("access_token: {}", jwtResponse.message());

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
        }
        //settings.setDynamicAttributeToken(dynamicAttributeToken);
        return token;
    }

    @Override
    /**
     * Send request towards Orbiter to validate if token is correct
     */
    public boolean validateToken(String tokenValue) {
        boolean isValid = false;

        logger.debug("Validating Orbiter token");
        
		try {
			Map<String, String> jsonObject = new HashMap<>();
            jsonObject.put("token", tokenValue);
            Gson gson = new GsonBuilder().create();
            String jsonString = gson.toJson(jsonObject);
            RequestBody formBody = RequestBody.create(JSON, jsonString); // new
	            
			//@formatter:off
			Request requestDaps = new Request.Builder()
					.url(dapsUrl + "/validate")
					.header("Host", "ecc-receiver")
					.header("accept", "application/json")
					.header("Content-Type", "application/json")
					.post(formBody)
					.build();
			//@formatter:on
			
			Response jwtResponse = client.newCall(requestDaps).execute();
			
			ResponseBody responseBody = jwtResponse.body();
			String response = responseBody.string();
			if (!jwtResponse.isSuccessful()) {
				logger.warn("Token did not validated successfuly", jwtResponse);
				throw new IOException("Error calling validate token." + jwtResponse);
			}
			
			logger.info("Response body of validate token request:\n{}", response);
			// parse body and check if content is like following
//			{
//			    "response": true,
//			    "description": "Token successfully validated"
//			}
//			otherwise we will get 'invalid token'
			try {
				ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);
				if(node.has("response") && node.get("response").asBoolean()) {
					logger.info("Token successfuly validated - signature OK");
					isValid = true;
				}
			} catch ( JsonProcessingException ex) {
				logger.info("Token was not validated correct");
			}
		} catch (Exception e) {
			logger.error(e);
		}
        return isValid;
    }
   
	
}