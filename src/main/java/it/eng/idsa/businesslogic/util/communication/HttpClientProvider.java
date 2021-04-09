package it.eng.idsa.businesslogic.util.communication;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Deprecated
public class HttpClientProvider {

	private static final Logger logger = LogManager.getLogger(HttpClientProvider.class);

	private static CloseableHttpClient httpClient;
	private KeyStore keystore;
	private KeyStore trustManagerKeyStore;
	
	@Value("${camel.component.jetty.use-global-ssl-context-parameters}")
	private boolean isSSLEnabled;
	
	@Value("${application.targetDirectory}") 
	private Path targetDirectory;
	@Value("${application.keyStoreName}") 
	private String keyStoreName;
	@Value("${application.keyStorePassword}") 
	private String keyStorePassword;
	@Value("${application.trustStoreName}") 
	private String trustStoreName;
	@Value("${application.trustStorePassword}") 
	private String trustStorePwd;
	
	@Value("${application.disableSslVerification:false}") 
	private boolean disableSslVerification;
	
	public void reset() throws IOException {
		httpClient.close();
		httpClient = null;
	}
	
	public CloseableHttpClient get() {
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
			HostnameVerifier hostnameVerifier;

			if(disableSslVerification) {
				logger.info("Creating HTTPClient without ssl validation");
				hostnameVerifier = NoopHostnameVerifier.INSTANCE;
				sslcontextBuilder.loadTrustMaterial(null, (cert, auth) -> true);

			} else {
				logger.info("Creating HTTPClient with ssl validation");
				hostnameVerifier = new DefaultHostnameVerifier();
				// let's hope that here we will have SSL context loaded correctly :D
				
				logger.info("Loading key store: " + keyStoreName);
				InputStream jksKeyStoreInputStream = Files.newInputStream(targetDirectory.resolve(keyStoreName));
				keystore = KeyStore.getInstance("JKS");
				keystore.load(jksKeyStoreInputStream, keyStorePassword.toCharArray());
				
				logger.info("Loading trust store: " + trustStoreName);
				InputStream jksTrustStoreInputStream = Files.newInputStream(targetDirectory.resolve(trustStoreName));
				trustManagerKeyStore = KeyStore.getInstance("JKS");
				trustManagerKeyStore.load(jksTrustStoreInputStream, trustStorePwd.toCharArray());
				sslcontextBuilder.loadTrustMaterial(trustManagerKeyStore, null);
				
				jksKeyStoreInputStream.close();
				jksTrustStoreInputStream.close();
			}

			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
					sslcontextBuilder.build(), hostnameVerifier);
			
			httpClient = HttpClients.custom()
					.setSSLSocketFactory((LayeredConnectionSocketFactory) sslConnectionSocketFactory).build();
			logger.info("Created Http Client with IDS truststore.");
			return httpClient;
		} catch (java.security.GeneralSecurityException | IOException e) {
			logger.error("Error in creating Http Client: ", e);
			return null;
		}
	}
}
