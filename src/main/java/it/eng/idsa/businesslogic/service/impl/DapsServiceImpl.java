package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.DapsService;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Implementation for managing DAPS.
 */
@Service
@Transactional
public class DapsServiceImpl implements DapsService {

	private static final Logger logger = LogManager.getLogger(DapsServiceImpl.class);

	@Autowired
	private ApplicationConfiguration configuration;

	private Key privKey;
	private Certificate cert;
	private PublicKey publicKey;
	private String token = "";

	@Override
	public String getJwtToken() {

		logger.debug("Get properties");
		Path targetDirectory = Paths.get(configuration.getTargetDirectory());
		String dapsUrl = configuration.getDapsUrl();
		String keyStoreName = configuration.getKeyStoreName();
		String keyStorePassword = configuration.getKeyStorePassword();
		String keystoreAliasName = configuration.getKeystoreAliasName();
		String connectorUUID = configuration.getConnectorUUID();
		String proxyUser = configuration.getProxyUser();
		String proxyPassword = configuration.getProxyPassword();
		String proxyHost = configuration.getProxyHost();
		String proxyPort = configuration.getProxyPort();

		try {
			logger.debug("Started get JWT token");
			InputStream jksInputStream = Files.newInputStream(targetDirectory.resolve(keyStoreName));
			KeyStore store = KeyStore.getInstance("JKS");
			store.load(jksInputStream, keyStorePassword.toCharArray());
			// get private key
			privKey = (PrivateKey) store.getKey(keystoreAliasName, keyStorePassword.toCharArray());
			// Get certificate of public key
			cert = store.getCertificate(keystoreAliasName);
			// Get public key
			publicKey = cert.getPublicKey();
			byte[] encodedPublicKey = publicKey.getEncoded();
			String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);

			// create signed JWT (JWS)
			// Create expiry date one day (86400 seconds) from now
			Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
			JwtBuilder jwtb = Jwts.builder().setIssuer(connectorUUID).setSubject(connectorUUID)
					.setExpiration(expiryDate).setIssuedAt(Date.from(Instant.now()))
					.setAudience("https://api.localhost").setNotBefore(Date.from(Instant.now()));

			String jws = jwtb.signWith(SignatureAlgorithm.RS256, privKey).compact();
			String json = "{\"\": \"\"," + "\"\":\"urn:ietf:params:oauth:client-assertion-type:jwt-bearer\","
					+ "\"\":\"" + jws + "\"," + "\"\":\"\"," + "}";

			Authenticator proxyAuthenticator = new Authenticator() {
				@Override
				public Request authenticate(Route route, Response response) throws IOException {
					String credential = Credentials.basic(proxyUser, proxyPassword);
					return response.request().newBuilder().header("Proxy-Authorization", credential).build();
				}
			};

			OkHttpClient client = null;
			if (!proxyUser.equalsIgnoreCase("")) {
				client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
						.writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
						.proxy(new Proxy(Proxy.Type.HTTP,
								new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))))
						.proxyAuthenticator(proxyAuthenticator).build();
			}
			else {
				client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
						.writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
			}
			RequestBody formBody = new FormBody.Builder().add("grant_type", "client_credentials")
					.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
					.add("client_assertion", jws).build();

			Request requestDaps = new Request.Builder().url(dapsUrl).post(formBody).build();
			Response responseDaps = client.newCall(requestDaps).execute();

			String body = responseDaps.body().string();
			ObjectNode node = new ObjectMapper().readValue(body, ObjectNode.class);

			if (node.has("access_token")) {
				token = node.get("access_token").asText();
				logger.info("access_token: {}", () -> token.toString());
			}
			logger.info("access_token: {}", () -> responseDaps.toString());
			logger.info("access_token: {}", () -> body);
			logger.info("access_token: {}", () -> responseDaps.message());

			if (!responseDaps.isSuccessful())
				throw new IOException("Unexpected code " + responseDaps);

			return token;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO: Get Token, return it
		return token;
	}

}
