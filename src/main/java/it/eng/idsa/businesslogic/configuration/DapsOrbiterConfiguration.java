package it.eng.idsa.businesslogic.configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.OkHttpClient;

@Configuration
public class DapsOrbiterConfiguration {
	
	@Bean
	public OkHttpClient getClient() throws KeyManagementException, NoSuchAlgorithmException {
		final TrustManager[] trustAllCerts = createTrustCertificates();
		final SSLSocketFactory sslSocketFactory = sslSocketFactory(trustAllCerts);
		OkHttpClient client = createHttpClient(trustAllCerts, sslSocketFactory);
		return client;
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
	
	private SSLSocketFactory sslSocketFactory(final TrustManager[] trustAllCerts)
			throws NoSuchAlgorithmException, KeyManagementException {
		final SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		// Create an ssl socket factory with our all-trusting manager
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		return sslSocketFactory;
	}


}
