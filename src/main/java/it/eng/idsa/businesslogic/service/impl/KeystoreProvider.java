package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

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
	private KeyManagerFactory kmf;
	private TrustManagerFactory trustFactory;
	
	public KeystoreProvider(@Value("${application.targetDirectory}") Path targetDirectory,
							@Value("${application.keyStoreName}") String keyStoreName, 
							@Value("${application.keyStorePassword}") String keyStorePassword, 
							@Value("${application.keystoreAliasName}") String keystoreAliasName,
							@Value("${application.trustStoreName:}") String trustStoreName, 
							@Value("${application.trustStorePassword}") String trustStorePwd,
							@Value("${application.disableSslVerification:false}") boolean disableSslVerification) {
		
		keyStorePwd = keyStorePassword;
		keystoreAlias = keystoreAliasName;
		
		InputStream jksTrustStoreInputStream = null;
		
		try {
			InputStream jksKeyStoreInputStream = Files.newInputStream(targetDirectory.resolve(keyStoreName));
			keystore = KeyStore.getInstance("JKS");
			logger.info("Loading key store: " + keyStoreName);
			keystore.load(jksKeyStoreInputStream, keyStorePassword.toCharArray());
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keystore, keyStorePassword.toCharArray());
			
			logger.info("Loading trust store: " + trustStoreName);
			if (!disableSslVerification) {
				jksTrustStoreInputStream = Files.newInputStream(targetDirectory.resolve(trustStoreName));
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

				kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(keystore, keyStorePassword.toCharArray());
				trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustFactory.init(trustManagerKeyStore);
				jksTrustStoreInputStream.close();
			}
			
			jksKeyStoreInputStream.close();
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
			logger.error("Error while loading keystore and truststore, {}", e);
		}
	}
	
	public KeyManagerFactory getKeystoreFactory() {
		return kmf;
	}
	
	public TrustManagerFactory getTrustManagerFactory() {
		return trustFactory;
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
