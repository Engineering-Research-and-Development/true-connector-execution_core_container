package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransportCertsManager {

	private static final Logger logger = LoggerFactory.getLogger(TransportCertsManager.class);

	private Map<String, String> transportCerts;
	private String connectorTransportCertSha;

	public TransportCertsManager(@Value("${application.ssl.key-store.name}") String sslKeystore,
			@Value("${application.ssl.key-store-password}") String sslPassword,
			@Value("${server.ssl.key-alias}") String sslAlias,
			@Value("${application.targetDirectory}") Path targetDirectory,
			@Value("${application.trustStoreName}") String trustStoreName,
			@Value("${application.trustStorePassword}") String trustStorePwd)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		InputStream jksTrustStoreInputStream = null;
		KeyStore trustManagerKeyStore;
		try {
			if (StringUtils.isNotBlank(trustStoreName)) {
				logger.info("TransportCerts manager initialization");
				jksTrustStoreInputStream = Files.newInputStream(targetDirectory.resolve(trustStoreName));
				trustManagerKeyStore = KeyStore.getInstance("JKS");
				trustManagerKeyStore.load(jksTrustStoreInputStream, trustStorePwd.toCharArray());
				populateTransportCertsSha256(trustManagerKeyStore);
				this.connectorTransportCertSha = getConnectorTransportCertSha256(targetDirectory, sslKeystore, sslPassword, sslAlias);
			} else {
				logger.info("Truststore not configured, cannot calculate TransportCertsSha256");
			}
		} finally {
			if (jksTrustStoreInputStream != null) {
				jksTrustStoreInputStream.close();
			}
		}
	}

	public String getConnectorTransportCetsSHa() {
		return this.connectorTransportCertSha;
	}

	public boolean isTransportCertsValid(String connectorId, String transportCert) {
		logger.info("Validating transportCertSha256 '{}' for connector with id {}", transportCert, connectorId);
		return transportCert.equals(transportCerts.get(connectorId));
	}

	private void populateTransportCertsSha256(KeyStore trustManagerKeyStore) throws KeyStoreException {
		logger.info("Calculating TransportCertsSha256 from configured truststore");
		transportCerts = new HashMap<>();
		List<String> aliases = Collections.list(trustManagerKeyStore.aliases());
		aliases.forEach(a -> {
			try {
				X509Certificate x509Cert = (X509Certificate) trustManagerKeyStore.getCertificate(a);
				x509Cert.getSubjectAlternativeNames().stream()
						.forEach(san -> transportCerts.put((String) san.get(1), getCertificateDigest(x509Cert)));
			} catch (KeyStoreException | CertificateParsingException e) {
				logger.error("Error while calculating TransportCertsSha256", e);
			}
		});
		transportCerts.remove("localhost");
		logger.info("Calculated {} transportCertsSha256 values", transportCerts.entrySet().size());
		if (logger.isDebugEnabled()) {
			transportCerts.keySet().stream().forEach(k -> logger.debug(k + " : " + transportCerts.get(k)));
		}
	}

	private String getCertificateDigest(Certificate cert) {
		String digest = null;
		try {
			byte[] bytes = MessageDigest.getInstance("SHA-256").digest(cert.getEncoded());
			digest = Hex.encodeHexString(bytes).toLowerCase();
		} catch (NullPointerException | CertificateEncodingException | NoSuchAlgorithmException e) {
			logger.error("Error while trying to load certificate", e);
		}
		return digest;
	}

	private String getConnectorTransportCertSha256(Path targetDirectory, String sslKeystore, String sslPassword, String sslAlias) {
		return getCertificateDigest(getCertificateTLS(targetDirectory, sslKeystore, sslPassword, sslAlias));
	}

	private X509Certificate getCertificateTLS(Path targetDirectory, String sslKeystore, String sslPassword, String sslAlias) {
		KeyStore keystore;
		try {
			InputStream jksKeyStoreInputStream = Files.newInputStream(targetDirectory.resolve(sslKeystore));
			keystore = KeyStore.getInstance("JKS");
			keystore.load(jksKeyStoreInputStream, sslPassword.toCharArray());
			return (X509Certificate) keystore.getCertificate(sslAlias);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			logger.error("Error while trying to read server certificate", e);
		}
		return null;
	}

}
