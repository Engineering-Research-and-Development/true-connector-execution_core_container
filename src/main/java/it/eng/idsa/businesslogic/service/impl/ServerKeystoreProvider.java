package it.eng.idsa.businesslogic.service.impl;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ServerKeystoreProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerKeystoreProvider.class);

	private KeyStore keystore;
	private String serverKeystoreAlias;
	
	public ServerKeystoreProvider(@Value("${application.targetDirectory}") Path targetDirectory,
			@Value("${server.ssl.key-store}") String keyStoreName,
			@Value("${server.ssl.key-password}") String keyStorePassword,
			@Value("${server.ssl.key-alias}") String serverKeystoreAlias) {
		
		this.serverKeystoreAlias = serverKeystoreAlias;
		
		try {
			Path path = targetDirectory.resolve(keyStoreName);
			InputStream jksKeyStoreInputStream;
			logger.info("Loading key store: {}", keyStoreName);
			if(path.isAbsolute()) {
				jksKeyStoreInputStream = Files.newInputStream(path);
			} else {
				jksKeyStoreInputStream = new ClassPathResource(path.toString()).getInputStream();
			}
			keystore = KeyStore.getInstance("JKS");
			keystore.load(jksKeyStoreInputStream, keyStorePassword.toCharArray());
			
		} catch (Exception e) {
			logger.error("Could not load server certificate", e);
		}
	}
	
	public X509Certificate getServerCertificate() {
		try {
			return (X509Certificate) keystore.getCertificate(serverKeystoreAlias);
		} catch (KeyStoreException e) {
			logger.error("Error while trying to get certificate key from keystore, {}", e);
		}
		return null;
	}
}
