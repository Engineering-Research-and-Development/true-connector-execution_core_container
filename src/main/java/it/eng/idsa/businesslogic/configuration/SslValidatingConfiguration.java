package it.eng.idsa.businesslogic.configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.service.impl.TLSProvider;

@Configuration
public class SslValidatingConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(SslValidatingConfiguration.class);

	@Autowired
	private TLSProvider tlsProvider;

	@PostConstruct
	/**
	 * Needed because URLJwsProvider uses URL.openConnection() and we need to set trustStore
	 * @throws KeyManagementException KeyManagementException
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmExceptio
	 */
	public void setupSSL() throws KeyManagementException, NoSuchAlgorithmException {
		logger.info("Using default ssl validation");
		SSLContext sslCtx = SSLContext.getInstance("TLSv1.3");
		sslCtx.init(tlsProvider.getKeyManagers(),
				tlsProvider.getTrustManagers(), new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
	}
}
