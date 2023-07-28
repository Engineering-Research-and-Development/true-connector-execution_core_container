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
import java.util.UUID;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class DapsKeystoreProvider {

	private static final Logger logger = LoggerFactory.getLogger(DapsKeystoreProvider.class);

	private KeyStore dapsKeystore;
	private String keyStorePwd;
	private String keystoreAlias;
	private boolean isEnabledDapsInteraction;

	public DapsKeystoreProvider(@Value("${application.targetDirectory}") Path targetDirectory,
			@Value("${application.keyStoreName}") String dapsKeyStoreName,
			@Value("${application.keyStorePassword}") String dapsKeyStorePassword,
			@Value("${application.keystoreAliasName}") String dapsKeystoreAliasName,
			@Value("${application.isEnabledDapsInteraction}") boolean isEnabledDapsInteraction) {

		this.isEnabledDapsInteraction = isEnabledDapsInteraction;
		keyStorePwd = dapsKeyStorePassword;
		keystoreAlias = dapsKeystoreAliasName;

		if (isEnabledDapsInteraction) {
			InputStream jksKeyStoreInputStream = null;
			try {
				dapsKeystore = KeyStore.getInstance("JKS");
				logger.info("Loading key store: " + dapsKeyStoreName);
				Path path = targetDirectory.resolve(dapsKeyStoreName);
				if (path.isAbsolute()) {
					jksKeyStoreInputStream = Files.newInputStream(path);
				} else {
					jksKeyStoreInputStream = new ClassPathResource(path.toString()).getInputStream();
				}
				jksKeyStoreInputStream = Files.newInputStream(targetDirectory.resolve(dapsKeyStoreName));
				dapsKeystore.load(jksKeyStoreInputStream, dapsKeyStorePassword.toCharArray());
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(dapsKeystore, dapsKeyStorePassword.toCharArray());
			} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException
					| UnrecoverableKeyException e) {
				logger.error("Error while loading keystore and/or truststore. DAPS interaction will not work.");
			} finally {
				if (jksKeyStoreInputStream != null) {
					try {
						jksKeyStoreInputStream.close();
					} catch (IOException e) {
						logger.error("Error while closing file.");
					}
				}
			}
		} else {
			logger.info("**********************************************************************");
			logger.info("DAPS Interaction disabled. KeyStore and/or trustStore not loaded");
			logger.info("**********************************************************************");
			logger.info("**********************************************************************");
			logger.info("Clearing house Interaction also disabled because of DAPS.");
			logger.info("**********************************************************************");
		}
	}

	public Key getPrivateKey() {
		try {
			return dapsKeystore.getKey(keystoreAlias, keyStorePwd.toCharArray());
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			logger.error("Error while trying to get private key from keystore, {}", e);
		}
		return null;
	}

	public X509Certificate getCertificate() {
		if (dapsKeystore == null || !isEnabledDapsInteraction) {
			logger.info("Keystore not initialized");
			return null;
		}
		try {
			return (X509Certificate) dapsKeystore.getCertificate(keystoreAlias);
		} catch (KeyStoreException e) {
			logger.error("Error while trying to get certificate key from keystore, {}", e);
		}
		return null;
	}

	public String getCertificateSubject() {
		if (dapsKeystore == null || !isEnabledDapsInteraction) {
			logger.info("Keystore not initialized");
			return UUID.randomUUID().toString();
		}
		return Arrays.stream(getCertificate().getSubjectX500Principal().getName().split(","))
				.filter(role -> role.contains("CN")).map(val -> val.split("=")[1].trim())
				.collect(Collectors.joining(","));
	}
}
