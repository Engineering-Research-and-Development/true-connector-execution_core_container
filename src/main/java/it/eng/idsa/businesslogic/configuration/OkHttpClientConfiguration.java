package it.eng.idsa.businesslogic.configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.service.impl.TLSProvider;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;

@Configuration
public class OkHttpClientConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(OkHttpClientConfiguration.class);

	@Autowired
	private TLSProvider tlsProvider;

	@Bean
	public OkHttpClient getClient() throws KeyManagementException, NoSuchAlgorithmException {
		return createHttpClient();
	}

	private OkHttpClient createHttpClient() throws KeyManagementException, NoSuchAlgorithmException {
		OkHttpClient client;
		TrustManager[] trustManagers = null;
		SSLSocketFactory sslSocketFactory;
		HostnameVerifier hostnameVerifier;

		logger.info("Using default SSL Validation with certificates from trust store");
		hostnameVerifier = OkHostnameVerifier.INSTANCE;
		trustManagers = tlsProvider.getTrustManagers();
		sslSocketFactory = sslSocketFactory(trustManagers);

		//@formatter:off
		client = new OkHttpClient.Builder()
				.connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))
				.connectTimeout(60, TimeUnit.SECONDS)
		        .writeTimeout(60, TimeUnit.SECONDS)
		        .readTimeout(60, TimeUnit.SECONDS)
		        .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0])
		        .hostnameVerifier(hostnameVerifier)
		        .build();
		//@formatter:on
		return client;
	}

	private SSLSocketFactory sslSocketFactory(final TrustManager[] trustManagers)
			throws NoSuchAlgorithmException, KeyManagementException {
		final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
		sslContext.init(null, trustManagers, new java.security.SecureRandom());
		// Create an ssl socket factory with our all-trusting manager
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		return sslSocketFactory;
	}
}
