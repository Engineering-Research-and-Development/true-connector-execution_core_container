package it.eng.idsa.businesslogic.service.impl;

import java.net.URL;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Service
@ConditionalOnExpression("!'${application.dapsVersion}'.equals('orbiter')")
public class DapsUtilityProvider {
    private static final Logger logger = LoggerFactory.getLogger(DapsUtilityProvider.class);

    @Autowired
    private DapsKeystoreProvider keystoreProvider;
    
	@Value("${application.dapsJWKSUrl}")
	private URL dapsJWKSUrl;
	
    private String targetAudience = "idsc:IDS_CONNECTORS_ALL";
    
	public String getDapsV2Jws() {
    	logger.debug("V2");

		String connectorUUID = getConnectorUUID();
		// create signed JWT (JWS)
		// Create expiry date one day (86400 seconds) from now
		Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
		String jws = null;
		try {
			Algorithm algorithm = Algorithm.RSA256(null,  (RSAPrivateKey) keystoreProvider.getPrivateKey());
			jws = JWT.create()
					.withIssuer(connectorUUID)
					.withSubject(connectorUUID)
					.withClaim("@context", "https://w3id.org/idsa/contexts/context.jsonld")
					.withClaim("@type", "ids:DatRequestToken")
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
	
	public Certificate getCertificate() {
		return keystoreProvider.getCertificate();
	}
	
	public PublicKey getPublicKey() {
		return keystoreProvider.getCertificate().getPublicKey();
	}
    
    public Algorithm provideAlgorithm(String tokenValue) {
    	DecodedJWT jwt = JWT.decode(tokenValue);
    	JwkProvider provider = new UrlJwkProvider(dapsJWKSUrl);
		Jwk jwk;
		Algorithm algorithm = null;
		try {
			jwk = provider.get(jwt.getKeyId());
			algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
		} catch (JwkException e) {
			logger.error("Error while trying to validate token {}", e);
		}
		return algorithm;
    }
    
	public String getConnectorUUID() {
		 // Get AKI
        //GET 2.5.29.14	SubjectKeyIdentifier / 2.5.29.35	AuthorityKeyIdentifier
        String aki_oid = Extension.authorityKeyIdentifier.getId();
        byte[] rawAuthorityKeyIdentifier = keystoreProvider.getCertificate().getExtensionValue(aki_oid);
        ASN1OctetString akiOc = ASN1OctetString.getInstance(rawAuthorityKeyIdentifier);
        AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.getInstance(akiOc.getOctets());
        byte[] authorityKeyIdentifier = aki.getKeyIdentifier();

        //GET SKI
        String ski_oid = Extension.subjectKeyIdentifier.getId();
        byte[] rawSubjectKeyIdentifier = keystoreProvider.getCertificate().getExtensionValue(ski_oid);
        ASN1OctetString ski0c = ASN1OctetString.getInstance(rawSubjectKeyIdentifier);
        SubjectKeyIdentifier ski = SubjectKeyIdentifier.getInstance(ski0c.getOctets());
        byte[] subjectKeyIdentifier = ski.getKeyIdentifier();

        String aki_result = beautifyHex(encodeHexString(authorityKeyIdentifier).toUpperCase());
        String ski_result = beautifyHex(encodeHexString(subjectKeyIdentifier).toUpperCase());

        String connectorUUID = ski_result + "keyid:" + aki_result.substring(0, aki_result.length() - 1);

        return connectorUUID;
	}
	
	   /**
     * Encode a byte array to an hex string
     * @param byteArray
     * @return
     */
    private String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }
    
    /**
     * Convert byte array to hex without any dependencies to libraries.
     * @param num
     * @return
     */
    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }
    
    /***
     * Beautyfies Hex strings and will generate a result later used to create the client id (XX:YY:ZZ)
     * @param hexString HexString to be beautified
     * @return beautifiedHex result
     */
    private String beautifyHex(String hexString) {
        String[] splitString = split(hexString, 2);
        StringBuffer sb = new StringBuffer();
        for(int i =0; i < splitString.length; i++) {
            sb.append(splitString[i]);
            sb.append(":");
        }
        return sb.toString();
    }
    

    /***
     * Split string ever len chars and return string array
     * @param src
     * @param len
     * @return
     */
    private String[] split(String src, int len) {
        String[] result = new String[(int)Math.ceil((double)src.length()/(double)len)];
        for (int i=0; i<result.length; i++)
            result[i] = src.substring(i*len, Math.min(src.length(), (i+1)*len));
        return result;
    }
}