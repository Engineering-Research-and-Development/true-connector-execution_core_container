package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TLSProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(TLSProvider.class);

	private KeyStore tlsKeystore;
	private KeyStore trustManagerKeyStore;
	private KeyManagerFactory keyFactory;
	private TrustManagerFactory trustFactory;
	
	@Value("${application.ssl.key-store.name}") String tlsKeystoreName;
	@Value("${application.ssl.key-store-password}") String tlsKeystorePassword;
	@Value("${server.ssl.key-alias}") String tlsKeystoreAlias;
	@Value("${application.targetDirectory}") Path targetDirectory;
	@Value("${application.trustStoreName}") String trustStoreName;
	@Value("${application.trustStorePassword}") String trustStorePwd;
	
	@PostConstruct
	private void loadKeyStores() {
		loadTLSKeystore();
		loadTrustStore();
	}

	private void loadTLSKeystore() {
		logger.info("Loading TLS keystore: " + tlsKeystoreName);
		try (InputStream jksKeyStoreInputStream = Files.newInputStream(targetDirectory.resolve(tlsKeystoreName))) {
			tlsKeystore = KeyStore.getInstance("JKS");
			tlsKeystore.load(jksKeyStoreInputStream, tlsKeystorePassword.toCharArray());
			keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyFactory.init(tlsKeystore, tlsKeystorePassword.toCharArray());
		} catch (UnrecoverableKeyException | IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			logger.error("Error while trying to read server certificate", e);
		} 
	}

	private void loadTrustStore() {
		logger.info("Loading truststore: " + trustStoreName);
		try (InputStream jksTrustStoreInputStream = Files.newInputStream(targetDirectory.resolve(trustStoreName))) {
			trustManagerKeyStore = KeyStore.getInstance("JKS");
			trustManagerKeyStore.load(jksTrustStoreInputStream, trustStorePwd.toCharArray());
			trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustFactory.init(trustManagerKeyStore);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			logger.error("Error while trying to read server truststore", e);
		} 
	}
	
	public KeyStore getTrustManagerKeyStore() {
		return trustManagerKeyStore;
	}
	
	public Enumeration<String> getTruststoreAliases() {
		try {
			return trustManagerKeyStore.aliases();
		} catch (KeyStoreException e) {
			logger.error("Could not read aliases from truststrore");
		}
		return Collections.emptyEnumeration();
	}
	
	public Certificate getTLSKeystoreCertificate() {
		try {
			return tlsKeystore.getCertificate(tlsKeystoreAlias);
		} catch (KeyStoreException e) {
			logger.error("Error while trying to read server certificate", e);
		}
		return null;
	}
	
	public KeyManager[] getKeyManagers() {
		return keyFactory.getKeyManagers();
	}
	
	public TrustManager[] getTrustManagers() {
		return trustFactory.getTrustManagers();
	}
	
	public X509Certificate getCertificateTLS() {
		try {
			return (X509Certificate) tlsKeystore.getCertificate(tlsKeystoreAlias);
		} catch (KeyStoreException e) {
			logger.error("Error while trying to read server certificate", e);
		}
		return null;
	}
	
}
