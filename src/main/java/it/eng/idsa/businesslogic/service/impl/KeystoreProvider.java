package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeystoreProvider {
	
    private static final Logger logger = LoggerFactory.getLogger(KeystoreProvider.class);

	private KeyStore keystore;
	private KeyStore trustManagerKeyStore;
	private String keyStorePwd;
	private String keystoreAlias;

	public KeystoreProvider(@Value("${application.targetDirectory}") Path targetDirectory,
							@Value("${application.keyStoreName}") String keyStoreName, 
							@Value("${application.keyStorePassword}") String keyStorePassword, 
							@Value("${application.keystoreAliasName}") String keystoreAliasName) {
		
		keyStorePwd = keyStorePassword;
		keystoreAlias = keystoreAliasName;
		
		try {
			InputStream jksKeyStoreInputStream = Files.newInputStream(targetDirectory.resolve(keyStoreName));
			InputStream jksTrustStoreInputStream = Files.newInputStream(targetDirectory.resolve(keyStoreName));
			
			keystore = KeyStore.getInstance("JKS");
			trustManagerKeyStore = KeyStore.getInstance("JKS");
			
			logger.info("Loading key store: " + keyStoreName);
			logger.info("Loading trust store: " + keyStoreName);
			keystore.load(jksKeyStoreInputStream, keyStorePassword.toCharArray());
			trustManagerKeyStore.load(jksTrustStoreInputStream, keyStorePassword.toCharArray());
			java.security.cert.Certificate[] certs = trustManagerKeyStore.getCertificateChain("ca");
			logger.info("Cert chain: " + Arrays.toString(certs));
			
			logger.info("LOADED CA CERT: " + trustManagerKeyStore.getCertificate("ca"));
			jksKeyStoreInputStream.close();
			jksTrustStoreInputStream.close();
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			logger.error("Error while loading keystore and truststore, {}", e);
		}
	}
	
	public Key getPrivateKey() {
		try {
			return keystore.getKey(keystoreAlias, keyStorePwd.toCharArray());
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			logger.error("Error while trying to get private key from keystore, {}", e);
		}
		return null;
	}
	
	public X509Certificate getCertificate() {
		try {
			return (X509Certificate) keystore.getCertificate(keystoreAlias);
		} catch (KeyStoreException e) {
			logger.error("Error while trying to get certificate key from keystore, {}", e);
		}
		return null;
	}
}
