package it.eng.idsa.businesslogic.configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.service.impl.KeystoreProvider;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;

@Configuration
public class OkHttpClientConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(OkHttpClientConfiguration.class);
	
	@Value("#{new Boolean('${application.disableSslVerification:false}')}")
	private boolean disableSslVerification;
	
	@Autowired
	private KeystoreProvider keystoreProvider;
	
	@Bean
	public OkHttpClient getClient() throws KeyManagementException, NoSuchAlgorithmException {
		return createHttpClient();
	}
	
	private OkHttpClient createHttpClient() throws KeyManagementException, NoSuchAlgorithmException {
		OkHttpClient client;
		TrustManager[] trustManagers = null;
		SSLSocketFactory sslSocketFactory;
		
		HostnameVerifier hostnameVerifier;
		if(disableSslVerification) {
			logger.info("Disabling SSL Validation");
			hostnameVerifier = new NoopHostnameVerifier();
			trustManagers = createTrustCertificates();
			sslSocketFactory = sslSocketFactory(trustManagers);
		} else {
			logger.info("Using default SSL Validation with certificates from trust store");
			hostnameVerifier = OkHostnameVerifier.INSTANCE;
			trustManagers = keystoreProvider.getTrustManagerFactory().getTrustManagers();
			sslSocketFactory = sslSocketFactory(trustManagers);
		}
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
	
	private SSLSocketFactory sslSocketFactory(final TrustManager[] trustManager)
			throws NoSuchAlgorithmException, KeyManagementException {
		final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
		sslContext.init(
				keystoreProvider.getKeystoreFactory().getKeyManagers(), 
				trustManager, 
				new java.security.SecureRandom());
		// Create an ssl socket factory with our all-trusting manager
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		return sslSocketFactory;
	}
}
