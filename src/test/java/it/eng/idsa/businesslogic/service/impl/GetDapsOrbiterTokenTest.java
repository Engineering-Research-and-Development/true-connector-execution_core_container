package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Disabled
public class GetDapsOrbiterTokenTest {

	private String eccReceiver = "2a62eda0-50bd-4640-9847-b0ea946f89bf";
	private String eccSender = "805f80f9-3170-4615-b80a-e93f2a4708e5";
	
	@Test
	public void generateJwTokenSender() {
		try {
			Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
            JwtBuilder jwtb =
                    Jwts.builder()
                            .claim("id", eccSender)
                            .setExpiration(expiryDate)
                            .setIssuedAt(Date.from(Instant.now()))
                            .setAudience("idsc:IDS_CONNECTORS_ALL")
                            .setNotBefore(Date.from(Instant.now()));
			String jws = jwtb.signWith(SignatureAlgorithm.RS256, getOrbiterPrivateKey("ecc-sender.key")).compact();
			System.out.println("Sender key:\n" + jws);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void generateJwTokenReceiver() {
		try {
			Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
            JwtBuilder jwtb =
                    Jwts.builder()
                            .claim("id", eccReceiver)
                            .setExpiration(expiryDate)
                            .setIssuedAt(Date.from(Instant.now()))
                            .setAudience("idsc:IDS_CONNECTORS_ALL")
                            .setNotBefore(Date.from(Instant.now()));
			String jws = jwtb.signWith(SignatureAlgorithm.RS256, getOrbiterPrivateKey("ecc-receiver.key")).compact();
			System.out.println("Receiver jwt:\n" + jws);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads Orbiter private key from file, removes header and footer and creates java PrivateKey object
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private PrivateKey getOrbiterPrivateKey(String keyFile) throws IOException, GeneralSecurityException {
		InputStream orbiterPrivateKeyInputStream = null;
		try {
			orbiterPrivateKeyInputStream = new ClassPathResource(keyFile).getInputStream();
			String privateKeyPEM = IOUtils.toString(orbiterPrivateKeyInputStream, StandardCharsets.UTF_8.name());
			privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
			privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
			byte[] encoded = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyPEM);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			return (PrivateKey) kf.generatePrivate(keySpec);
		} finally {
			if(orbiterPrivateKeyInputStream != null) {
				orbiterPrivateKeyInputStream.close();
			}
		}
	}
}
