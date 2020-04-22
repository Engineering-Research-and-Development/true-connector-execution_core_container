package it.eng.idsa.businesslogic.util.communication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public final class PemReader {

	private static final Logger logger = LogManager.getLogger(PemReader.class);

	private static final Pattern certPattern = Pattern.compile(
			"-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*CERTIFICATE[^-]*-+", 2);

	public static KeyStore createTruststore(String certificateChain) throws IOException, GeneralSecurityException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);

		List<X509Certificate> certificateList = readCertificateChain(certificateChain);
		for (X509Certificate certificate : certificateList) {
			X500Principal principal = certificate.getSubjectX500Principal();
			logger.info("Adding certificate to CA chain: {}", principal.getName("RFC2253"));
			keyStore.setCertificateEntry(principal.getName("RFC2253"), certificate);
		}
		return keyStore;
	}

	private static List<X509Certificate> readCertificateChain(String certificateChain) throws GeneralSecurityException {
		String content = new String(base64Decode(certificateChain), StandardCharsets.US_ASCII);
		Matcher matcher = certPattern.matcher(content);
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		List<X509Certificate> certificates = new ArrayList<>();

		int start = 0;
		while (matcher.find(start)) {
			byte[] buffer = base64Decode(matcher.group(1));
			certificates
					.add((X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(buffer)));
			start = matcher.end();
		}

		return certificates;
	}

	private static byte[] base64Decode(String base64) {
		return Base64.getMimeDecoder().decode(base64.getBytes(StandardCharsets.US_ASCII));
	}

}