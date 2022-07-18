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
import org.springframework.core.io.ClassPathResource;
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
			@Value("#{new Boolean('${application.disableSslVerification:false}')}") boolean disableSslVerification,
			@Value("${application.isEnabledDapsInteraction}") boolean isEnabledDapsInteraction) {

		keyStorePwd = keyStorePassword;
		keystoreAlias = keystoreAliasName;

		InputStream jksTrustStoreInputStream = null;

		try {
			Path path = targetDirectory.resolve(keyStoreName);
			InputStream jksKeyStoreInputStream;
			if(path.isAbsolute()) {
				jksKeyStoreInputStream = Files.newInputStream(path);
			} else {
				jksKeyStoreInputStream = new ClassPathResource(path.toString()).getInputStream();
			}
			keystore = KeyStore.getInstance("JKS");
			logger.info("Loading key store: " + keyStoreName);
			keystore.load(jksKeyStoreInputStream, keyStorePassword.toCharArray());
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keystore, keyStorePassword.toCharArray());

			if (!disableSslVerification) {
				logger.info("Loading trust store: " + trustStoreName);
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
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException
				| UnrecoverableKeyException e) {
			if (isEnabledDapsInteraction) {
				logger.error("Error while loading keystore and/or truststore. DAPS interaction will not work.", e);
			} else {
				logger.info("**********************************************************************");
				logger.info("DAPS Interaction disabled. KeyStore and/or trustStore not loaded");
				logger.info("**********************************************************************");
			}
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
		if(keystore == null) {
			logger.info("Keystore not initialized");
			return null;
		}
		try {
			return (X509Certificate) keystore.getCertificate(keystoreAlias);
		} catch (KeyStoreException e) {
			logger.error("Error while trying to get certificate key from keystore, {}", e);
		}
		return null;
	}
}
