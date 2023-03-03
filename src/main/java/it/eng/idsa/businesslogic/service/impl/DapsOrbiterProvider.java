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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.RSAKeyProvider;


@Component
@ConditionalOnProperty(name = "application.dapsVersion", havingValue = "orbiter")
public class DapsOrbiterProvider {
    private static final Logger logger = LoggerFactory.getLogger(DapsOrbiterProvider.class);

	
    @Value("${application.targetDirectory}")
    private Path targetDirectory;
    @Value("${application.daps.orbiter.privateKey}")
    private String dapsOrbiterPrivateKey;
    @Value("${application.connectorUUID}")
    private String connectorUUID;
    private String targetAudience = "idsc:IDS_CONNECTORS_ALL";

	/**
	 * Reads Orbiter private key from file, removes header and footer and creates java PrivateKey object
	 * @return PrivateKey
	 * @throws IOException ioException
	 * @throws GeneralSecurityException GeneralSecurityException
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
	 * Provide jws from Orbiter
	 * @return jws representation of Json Web Token 
	 * @throws IOException ioException 
	 * @throws GeneralSecurityException GeneralSecurityException
	 */
	public String provideJWS() throws IOException, GeneralSecurityException  {
		Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
		String jws = null;
		try {
			Algorithm algorithm = Algorithm.RSA256((RSAKeyProvider) getOrbiterPrivateKey());
			jws = JWT.create()
					.withClaim("id", connectorUUID)
					.withExpiresAt(expiryDate)
					.withIssuedAt(Date.from(Instant.now()))
					.withAudience(targetAudience)
					.withNotBefore(Date.from(Instant.now()))
					.sign(algorithm);
		} catch (JWTCreationException exception) {
			logger.error("Token creation error: {}", exception.getMessage());
		}
          return jws;
	}
}
