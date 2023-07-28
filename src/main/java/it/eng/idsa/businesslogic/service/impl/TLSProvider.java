package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	
	@Value("${application.ssl.key-store.name}") String sslKeystore;
	@Value("${application.ssl.key-store-password}") String sslPassword;
	@Value("${server.ssl.key-alias}") String sslAlias;
	@Value("${application.targetDirectory}") Path targetDirectory;
	@Value("${application.trustStoreName}") String trustStoreName;
	@Value("${application.trustStorePassword}") String trustStorePwd;
	
	@PostConstruct
	private void loadKeyStores() {
		loadTLSKeystore();
		loadTrustStore();
	}

	private void loadTLSKeystore() {
		logger.info("Loading key store: " + sslKeystore);
		try (InputStream jksKeyStoreInputStream = Files.newInputStream(targetDirectory.resolve(sslKeystore))) {
			tlsKeystore = KeyStore.getInstance("JKS");
			tlsKeystore.load(jksKeyStoreInputStream, sslPassword.toCharArray());
			keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyFactory.init(tlsKeystore, sslPassword.toCharArray());
		} catch (UnrecoverableKeyException | IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			logger.error("Error while trying to read server certificate", e);
		} 
		
	}

	private void loadTrustStore() {
		logger.info("Loading key store: " + trustStoreName);
		try (InputStream jksTrustStoreInputStream = Files.newInputStream(targetDirectory.resolve(trustStoreName))) {
			trustManagerKeyStore = KeyStore.getInstance("JKS");
			trustManagerKeyStore.load(jksTrustStoreInputStream, trustStorePwd.toCharArray());
			
			if (logger.isDebugEnabled()) {
				PKIXParameters params;
				try {
					params = new PKIXParameters(trustManagerKeyStore);
					Set<TrustAnchor> trustAnchors = params.getTrustAnchors();
					List<Certificate> certificates = trustAnchors.stream().map(TrustAnchor::getTrustedCert)
							.collect(Collectors.toList());
					logger.debug("Cert chain: " + certificates);
				} catch (InvalidAlgorithmParameterException e) {
					logger.error("Error while trying to read trusted certificates");
				}
			}
			List<String> aliases = Collections.list(trustManagerKeyStore.aliases());
			logger.info("Trusted certificate aliases: {}", aliases);
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
			return tlsKeystore.getCertificate(sslAlias);
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
			return (X509Certificate) tlsKeystore.getCertificate(sslAlias);
		} catch (KeyStoreException e) {
			logger.error("Error while trying to read server certificate", e);
		}
		return null;
	}
	
}
