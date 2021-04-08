package it.eng.idsa.businesslogic.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SslValidatingConfiguration {
	
	private static final Logger logger = LogManager.getLogger(SslValidatingConfiguration.class);

	private KeyStore keystore;
	private KeyStore trustManagerKeyStore;
	
	public SslValidatingConfiguration(
				@Value("${application.disableSslVerification:false}") boolean disableSslVerification,
				@Value("${application.targetDirectory}") Path targetDirectory,
				@Value("${application.keyStoreName}") String keyStoreName, 
				@Value("${application.keyStorePassword}") String keyStorePassword, 
				@Value("${application.trustStoreName}") String trustStoreName, 
				@Value("${application.trustStorePassword}") String trustStorePwd) 
					throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException {
		if (disableSslVerification) {
			logger.info("Disabling ssl validation");
			disableSslVerification();
		} else {
			logger.info("Using default ssl validation");
			setTrustStore(targetDirectory, keyStoreName, keyStorePassword, trustStoreName, trustStorePwd);
		}
	}

	/**
	 * Needed because URLJwsProvider uses URL.openConnection() and we need to set trustStore
	 * @param targetDirectory
	 * @param keyStoreName
	 * @param keyStorePassword
	 * @param trustStoreName
	 * @param trustStorePwd
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 */
	private void setTrustStore(Path targetDirectory, String keyStoreName, String keyStorePassword, 
			String trustStoreName, String trustStorePwd) throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException {
		SSLContext sslCtx = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = null;
		TrustManagerFactory trustFactory = null;
		try {
			InputStream jksKeyStoreInputStream = Files.newInputStream(targetDirectory.resolve(keyStoreName));
			keystore = KeyStore.getInstance("JKS");
			logger.info("Loading key store: " + keyStoreName);
			keystore.load(jksKeyStoreInputStream, keyStorePassword.toCharArray());
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keystore, keyStorePassword.toCharArray());
			
			logger.info("Loading trust store: " + trustStoreName);
			InputStream jksTrustStoreInputStream = Files.newInputStream(targetDirectory.resolve(trustStoreName));
			trustManagerKeyStore = KeyStore.getInstance("JKS");
			trustManagerKeyStore.load(jksTrustStoreInputStream, trustStorePwd.toCharArray());
			if(logger.isDebugEnabled()) {
				PKIXParameters params;
				try {
					params = new PKIXParameters(trustManagerKeyStore);
					Set<TrustAnchor> trustAnchors = params.getTrustAnchors();
					List<Certificate> certificates = trustAnchors.stream()
							.map(TrustAnchor::getTrustedCert)
							.collect(Collectors.toList());
					logger.debug("Cert chain: " + certificates);
				} catch (InvalidAlgorithmParameterException e) {
					logger.error("Error while trying to read trusted certificates");
				}
			}
			List<String> aliases = Collections.list(trustManagerKeyStore.aliases());
			logger.info("Trusted certificate aliases: {}", aliases);
			trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(trustManagerKeyStore);
			
			logger.info("LOADED CA CERT: " + trustManagerKeyStore.getCertificate("ca"));
			jksKeyStoreInputStream.close();
			jksTrustStoreInputStream.close();
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			logger.error("Error while loading keystore and truststore, {}", e);
		}
		
		sslCtx.init(kmf.getKeyManagers(), trustFactory.getTrustManagers(), new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
	}
	
	private void disableSslVerification() {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(new NoopHostnameVerifier());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

}
