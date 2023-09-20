package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;

import it.eng.idsa.businesslogic.service.DapsService;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */

@ConditionalOnExpression("'${application.isEnabledDapsInteraction}' == 'true' && '${application.dapsVersion}'=='v2'")
@Service
@Transactional
public class DapsV2ServiceImpl implements DapsService {
	private static final Logger logger = LoggerFactory.getLogger(DapsV2ServiceImpl.class);

	@Autowired
	private OkHttpClient client;
	
	@Autowired
	private DapsUtilityProvider dapsUtilityProvider;
	@Autowired
	private TransportCertsManager transportCertsManager;

	private String token = null;

	@Value("${application.dapsUrl}")
	private String dapsUrl;
	
	@Value("${application.dapsJWKSUrl}")
	private URL dapsJWKSUrl;
	
	@Value("${application.extendedTokenValidation}")
	private boolean extendedTokenValidation;
	
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
	public boolean validateToken(String tokenValue) {
		boolean valid = false;
		if (tokenValue == null) {
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
			if(extendedTokenValidation) {
				if(!extendedTokenValidation(jwt)) {
					valid = false;
				}
			}
		} catch (SignatureVerificationException e) {
			logger.info("Token did not get verified, {}", e);
		} catch (JWTDecodeException e) {
			logger.error("Invalid token, {}", e);
		}
		return valid;
	}

	private boolean extendedTokenValidation(DecodedJWT jwt) {
		String referringConnector = jwt.getClaim("referringConnector").asString();
		URI uri = URI.create(referringConnector);
		String connectorId = uri.getHost();

		boolean isValid = false;
		
		String transportCertsSha256 = jwt.getClaim("transportCertsSha256").asString();
		if(transportCertsSha256 != null) {
			logger.info("Single transportCertsSha256");
			isValid = transportCertsManager.isTransportCertValid(connectorId, transportCertsSha256);
		} else {
			logger.info("Multiple transportCertsSha256");
			String[] transportCerts = jwt.getClaim("transportCertsSha256").asArray(String.class);
			for (String transportCert : transportCerts) {
				if(transportCertsManager.isTransportCertValid(connectorId, transportCert)) {
					isValid = true;
					transportCertsSha256 = transportCert;
					break;
				}
			}
		}
		logger.info("TransportCertsSha256 for connector validated as {}", isValid);
		return isValid;
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
		return dapsUtilityProvider.getConnectorUUID();
	}
	
	@VisibleForTesting
	String getJwTokenInternal() {

		Response jwtResponse = null;
		try {
			logger.info("Retrieving Dynamic Attribute Token...");

			String jws = dapsUtilityProvider.getDapsV2Jws();

			// build form body to embed client assertion into post request
			Builder formBodyBuilder = new FormBody.Builder()
					.add("grant_type", "client_credentials")
					.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
					.add("client_assertion", jws)
					.add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL");
			
			if(extendedTokenValidation) { 
				String certsShaClaim = createCertsShaClaim();
				formBodyBuilder.add("claims", certsShaClaim);
			}

			RequestBody formBody = formBodyBuilder.build();
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
			ObjectNode node = new ObjectMapper().readValue(jwtString, ObjectNode.class);

			if (node.has("access_token")) {
				token = node.get("access_token").asText();
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

	private String createCertsShaClaim() {
		JSONArray transportCertsJsonArray = new JSONArray();
		transportCertsJsonArray.put(transportCertsManager.getCertificateDigest(dapsUtilityProvider.getCertificate()));
		transportCertsJsonArray.put(transportCertsManager.getConnectorTransportCertsSha());
		
		return new JSONObject()
	        .put("access_token", new JSONObject().put("transportCertsSha256", new JSONObject().put("value", transportCertsJsonArray)))
	        .toString();
	}
}