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
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
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

//    @Override
	/*
	 * public boolean validateToken1(String tokenValue) { boolean isValid = false;
	 * 
	 * logger.debug("Get properties");
	 * 
	 * try { // Set up a JWT processor to parse the tokens and then check their
	 * signature // and validity time window (bounded by the "iat", "nbf" and "exp"
	 * claims) ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new
	 * DefaultJWTProcessor<SecurityContext>();
	 * 
	 * // The public RSA keys to validate the signatures will be sourced from the //
	 * OAuth 2.0 server's JWK set, published at a well-known URL. The RemoteJWKSet
	 * // object caches the retrieved keys to speed up subsequent look-ups and can
	 * // also gracefully handle key-rollover JWKSource<SecurityContext> keySource =
	 * new RemoteJWKSet<SecurityContext>( new URL(dapsJWKSUrl));
	 * 
	 * // Load JWK set from URL JWKSet publicKeys = null; publicKeys =
	 * JWKSet.load(new URL(dapsJWKSUrl)); RSAKey key = (RSAKey)
	 * publicKeys.getKeyByKeyId("default");
	 * 
	 * // The expected JWS algorithm of the access tokens (agreed out-of-band)
	 * JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
	 * 
	 * // Configure the JWT processor with a key selector to feed matching public //
	 * RSA keys sourced from the JWK set URL JWSKeySelector<SecurityContext>
	 * keySelector = new JWSVerificationKeySelector<SecurityContext>(
	 * expectedJWSAlg, keySource); jwtProcessor.setJWSKeySelector(keySelector);
	 * 
	 * // Validate signature String exponentB64u =
	 * key.getPublicExponent().toString(); String modulusB64u =
	 * key.getModulus().toString();
	 * 
	 * // Build the public key from modulus and exponent PublicKey publicKey =
	 * DapsServiceImpl.getPublicKey(modulusB64u, exponentB64u);
	 * 
	 * // print key as PEM (base64 and headers) String publicKeyPEM =
	 * "-----BEGIN PUBLIC KEY-----\n" +
	 * Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "\n" +
	 * "-----END PUBLIC KEY-----";
	 * 
	 * logger.debug("publicKeyPEM: {}", publicKeyPEM);
	 * 
	 * //get signed data and signature from JWT String signedData =
	 * tokenValue.substring(0, tokenValue.lastIndexOf(".")); String signatureB64u =
	 * tokenValue.substring(tokenValue.lastIndexOf(".") + 1, tokenValue.length());
	 * byte signature[] = Base64.getUrlDecoder().decode(signatureB64u);
	 * 
	 * //verify Signature Signature sig = Signature.getInstance("SHA256withRSA");
	 * sig.initVerify(publicKey); sig.update(signedData.getBytes()); boolean v =
	 * sig.verify(signature); logger.debug("result_validation_signature = ", v);
	 * 
	 * if (v == false) { isValid = false; } else { // Process the token
	 * SecurityContext ctx = null; // optional context parameter, not required here
	 * JWTClaimsSet claimsSet = jwtProcessor.process(tokenValue, ctx);
	 * 
	 * logger.debug("claimsSet = ", claimsSet.toJSONObject());
	 * 
	 * isValid = true; }
	 * 
	 * } catch (Exception e) { logger.error(e.getMessage()); }
	 * 
	 * return isValid; }
	 */
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