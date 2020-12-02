package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class DapsOrbiterProvider {
	
    @Value("${application.targetDirectory}")
    private Path targetDirectory;
    @Value("${application.daps.orbiter.privateKey}")
    private String dapsOrbiterPrivateKey;
    @Value("${application.connectorUUID}")
    private String connectorUUID;
    private String targetAudience = "idsc:IDS_CONNECTORS_ALL";

	/**
	 * Reads Orbiter private key from file, removes header and footer and creates java PrivateKey object
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private PrivateKey getOrbiterPrivateKey() throws IOException, GeneralSecurityException {
		InputStream orbiterPrivateKeyInputStream = null;
		try {
			orbiterPrivateKeyInputStream = Files.newInputStream(targetDirectory.resolve(dapsOrbiterPrivateKey));
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
	
	/**
	 * Provide jws from Orbiter </br>
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public String provideJWS() throws IOException, GeneralSecurityException  {
		  Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
          JwtBuilder jwtb =
                  Jwts.builder()
                          .claim("id", connectorUUID)
                          .setExpiration(expiryDate)
                          .setIssuedAt(Date.from(Instant.now()))
                          .setAudience(targetAudience)
                          .setNotBefore(Date.from(Instant.now()));
          return jwtb.signWith(SignatureAlgorithm.RS256, getOrbiterPrivateKey()).compact();
	}
}
