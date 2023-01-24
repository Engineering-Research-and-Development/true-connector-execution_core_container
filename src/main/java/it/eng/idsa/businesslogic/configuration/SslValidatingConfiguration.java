package it.eng.idsa.businesslogic.configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.service.impl.KeystoreProvider;

@Configuration
public class SslValidatingConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(SslValidatingConfiguration.class);

	@Autowired
	private KeystoreProvider keystoreProvider;
	@Value("#{new Boolean('${application.disableSslVerification:false}')}")
	private boolean disableSslVerification;
	
	@PostConstruct
	public void setupSSL() throws KeyManagementException, NoSuchAlgorithmException {
		if (disableSslVerification) {
			logger.info("Disabling ssl validation");
			disableSslVerification();
		} else {
			logger.info("Using default ssl validation");
			setTrustStore();
		}
	}

	/**
	 * Needed because URLJwsProvider uses URL.openConnection() and we need to set trustStore
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	private void setTrustStore() throws NoSuchAlgorithmException, KeyManagementException {
		logger.info("Setting up truststore");
		SSLContext sslCtx = SSLContext.getInstance("TLS");
		sslCtx.init(keystoreProvider.getKeystoreFactory().getKeyManagers(), 
				keystoreProvider.getTrustManagerFactory().getTrustManagers(), 
				new java.security.SecureRandom());
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
