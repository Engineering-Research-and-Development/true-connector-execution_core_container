package it.eng.idsa.businesslogic.util.communication;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.idsa.businesslogic.util.config.keystore.AcceptAllTruststoreConfig;
import it.eng.idsa.businesslogic.util.config.keystore.TruststoreConfig;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class HttpClientGenerator {

	private static final Logger logger = LogManager.getLogger(HttpClientGenerator.class);

	private static CloseableHttpClient httpClient;

	public static void reset() throws IOException {
		httpClient.close();
		httpClient = null;
	}

	public static CloseableHttpClient get(TruststoreConfig trustStore, boolean isSSLEnabled) {
		if (httpClient != null) {
			return httpClient;
		}
		if(!isSSLEnabled) {
			httpClient =  HttpClients.custom().build();
			logger.info("Created Http Client without SSL.");
			return httpClient;
		}

		try {
			SSLContextBuilder sslcontextBuilder = SSLContexts.custom();

			if (trustStore instanceof AcceptAllTruststoreConfig) {
				sslcontextBuilder.loadTrustMaterial(null, (cert, auth) -> true);
			} else {
				sslcontextBuilder.loadTrustMaterial((TrustStrategy) null);
			}

			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
					sslcontextBuilder.build(), (HostnameVerifier) NoopHostnameVerifier.INSTANCE);
			httpClient = HttpClients.custom()
					.setSSLSocketFactory((LayeredConnectionSocketFactory) sslConnectionSocketFactory).build();
			logger.info("Created Http Client with IDS truststore.");
			return httpClient;
		} catch (java.security.GeneralSecurityException e) {
			logger.error("Error in creating Http Client: ", e);

			return null;
		}
	}
}
