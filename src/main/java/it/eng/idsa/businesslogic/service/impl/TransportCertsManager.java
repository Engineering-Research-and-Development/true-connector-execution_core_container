package it.eng.idsa.businesslogic.service.impl;

import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Reads certificate from truststore, \n calculates SHA256 using
 * MessageDigest.getInstance("SHA-256").digest(cert.getEncoded())\n creates map
 * with certificate SubjectAlternativeName and calculated SHA256\n
 * 
 * Map is later used to verify if jwt.transportCertSha256 matches with the one
 * from truststore
 * 
 * @author igor.balog
 *
 */
@Component
public class TransportCertsManager {

	private static final Logger logger = LoggerFactory.getLogger(TransportCertsManager.class);

	private Map<String, String> transportCerts;
	private String connectorTransportCertSha;
	private TLSProvider tlsProvider;

	public TransportCertsManager(TLSProvider tlsProvider) {
		this.tlsProvider = tlsProvider;
		populateTransportCertsSha();
		setConnectorTransportCertSha();
	}

	public String getConnectorTransportCertsSha() {
		return this.connectorTransportCertSha;
	}

	private void setConnectorTransportCertSha() {
		this.connectorTransportCertSha = getCertificateDigest(tlsProvider.getTLSKeystoreCertificate());
	}

	public boolean isTransportCertValid(String connectorId, String transportCert) {
		logger.info("Validating transportCertSha256 for connector");
		return transportCert.equals(transportCerts.get(connectorId));
	}

	private void populateTransportCertsSha() {
		logger.info("Calculating TransportCertsSha256 from configured truststore");
		transportCerts = new HashMap<>();
		List<String> aliases = Collections.list(tlsProvider.getTruststoreAliases());
		aliases.forEach(a -> {
			try {
				X509Certificate x509Cert = (X509Certificate) tlsProvider.getTrustManagerKeyStore().getCertificate(a);
				if (x509Cert.getSubjectAlternativeNames() != null) {
					x509Cert.getSubjectAlternativeNames().stream()
							.forEach(san -> transportCerts.put((String) san.get(1), getCertificateDigest(x509Cert)));
				}
			} catch (KeyStoreException | CertificateParsingException e) {
				logger.error("Error while calculating TransportCertsSha256", e);
			}
		});
		transportCerts.remove("localhost");
	}

	public String getCertificateDigest(Certificate cert) {
		String digest = null;
		try {
			byte[] bytes = MessageDigest.getInstance("SHA-256").digest(cert.getEncoded());
			digest = Hex.encodeHexString(bytes).toLowerCase();
		} catch (NullPointerException | CertificateEncodingException | NoSuchAlgorithmException e) {
			logger.error("Error while trying to load certificate", e);
		}
		return digest;
	}

}
