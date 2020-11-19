package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
    @Value("${application.targetDirectory}")
    private Path targetDirectory;
    @Value("${application.dapsUrl}")
    private String dapsUrl;
    @Value("${application.connectorUUID}")
    private String connectorUUID;

    @Value("${application.daps.orbiter.privateKey}")
    private String dapsOrbiterPrivateKey;
    @Value("${application.daps.orbiter.password}")
    private String dapsOrbiterPassword;

    @Override
    public String getJwtToken() {

        String targetAudience = "idsc:IDS_CONNECTORS_ALL";

        // Try clause for setup phase (loading keys, building trust manager)
        try {

            OkHttpClient client = null;
            final TrustManager[] trustAllCerts = createTrustCertificates();
            // Install the all-trusting trust manager
            final SSLSocketFactory sslSocketFactory = sslSocketFactory(trustAllCerts);
            client = createHttpClient(trustAllCerts, sslSocketFactory);

            logger.info("ConnectorUUID: " + connectorUUID);
            logger.info("Retrieving Dynamic Attribute Token...");

            // create signed JWT (JWS)
            // Create expiry date one day (86400 seconds) from now
            Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
            JwtBuilder jwtb =
                    Jwts.builder()
                            .claim("id", connectorUUID)
                            .setExpiration(expiryDate)
                            .setIssuedAt(Date.from(Instant.now()))
                            .setAudience(targetAudience)
                            .setNotBefore(Date.from(Instant.now()));
            //String jws = jwtb.signWith(privKey, SignatureAlgorithm.RS256).compact();
            String jws = jwtb.signWith(SignatureAlgorithm.RS256, getOrbiterPrivateKey()).compact();
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

            if (!jwtResponse.isSuccessful()) {
                throw new IOException("Unexpected code " + jwtResponse);
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
        }
        //settings.setDynamicAttributeToken(dynamicAttributeToken);
        return token;
    }


	private SSLSocketFactory sslSocketFactory(final TrustManager[] trustAllCerts)
			throws NoSuchAlgorithmException, KeyManagementException {
		final SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		// Create an ssl socket factory with our all-trusting manager
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		return sslSocketFactory;
	}


	private TrustManager[] createTrustCertificates() {
		final TrustManager[] trustAllCerts = new TrustManager[]{
		        new X509TrustManager() {
		            @Override
		            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
		                                           String authType) throws CertificateException {
		            }

		            @Override
		            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
		                                           String authType) throws CertificateException {
		            }

		            @Override
		            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		                return new java.security.cert.X509Certificate[0];
		            }
		        }
		};
		return trustAllCerts;
	}


	private OkHttpClient createHttpClient(final TrustManager[] trustAllCerts, final SSLSocketFactory sslSocketFactory) {
		OkHttpClient client;
		//@formatter:off
		client = new OkHttpClient.Builder()
				.connectTimeout(60, TimeUnit.SECONDS)
		        .writeTimeout(60, TimeUnit.SECONDS)
		        .readTimeout(60, TimeUnit.SECONDS)
		        .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
		        .hostnameVerifier(new HostnameVerifier() {
		            @Override
		            public boolean verify(String hostname, SSLSession session) {
		                return true;
		            }
		        })
		        .build();
		//@formatter:on
		return client;
	}


    @Override
    /**
     * Send request towards Orbiter to validate if token is correct
     */
    public boolean validateToken(String tokenValue) {
        boolean isValid = false;

        logger.debug("Validating Orbiter token");
        OkHttpClient client = null;
        
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
			
			final TrustManager[] trustAllCerts = createTrustCertificates();
			final SSLSocketFactory sslSocketFactory = sslSocketFactory(trustAllCerts);
			client = createHttpClient(trustAllCerts, sslSocketFactory);

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
    
	/**
	 * Reads Orbiter private key from file, removes header and footer and creates java PrivateKey object
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private PrivateKey getOrbiterPrivateKey() throws IOException, GeneralSecurityException {
		InputStream orbiterPrivateKeyInputStream = null;
		try {
			orbiterPrivateKeyInputStream = Files.newInputStream(targetDirectory.resolve(dapsOrbiterPrivateKey));
			String privateKeyPEM = IOUtils.toString(orbiterPrivateKeyInputStream, StandardCharsets.UTF_8.name());
			privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
			privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
			byte[] encoded = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyPEM);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			return (PrivateKey) kf.generatePrivate(keySpec);
		} finally {
			if(orbiterPrivateKeyInputStream != null) {
				orbiterPrivateKeyInputStream.close();
			}
		}
	}
	
}