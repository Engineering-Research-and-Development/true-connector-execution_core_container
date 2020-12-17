package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.eng.idsa.businesslogic.service.DapsService;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */

@ConditionalOnProperty(name="application.dapsVersion", havingValue="v2")
@Service
@Transactional
public class DapsV2ServiceImpl implements DapsService {
    private static final Logger logger = LoggerFactory.getLogger(DapsV2ServiceImpl.class);

    private SSLSocketFactory sslSocketFactory = null;
    private String token = "";
    @Value("${application.targetDirectory}")
    private Path targetDirectory;
    @Value("${application.dapsUrl}")
    private String dapsUrl;
    @Value("${application.keyStoreName}")
    private String keyStoreName;
    @Value("${application.keyStorePassword}")
    private String keyStorePassword;
    @Value("${application.keystoreAliasName}")
    private String keystoreAliasName;
    @Value("${application.dapsJWKSUrl}")
    private String dapsJWKSUrl;

    public String getJwtToken() {

        String targetAudience = "idsc:IDS_CONNECTORS_ALL";

        // Try clause for setup phase (loading keys, building trust manager)
        Response jwtResponse = null;
        try {
            InputStream jksKeyStoreInputStream =
                    Files.newInputStream(targetDirectory.resolve(keyStoreName));
            InputStream jksTrustStoreInputStream =
                    Files.newInputStream(targetDirectory.resolve(keyStoreName));

            KeyStore keystore = KeyStore.getInstance("JKS");
            KeyStore trustManagerKeyStore = KeyStore.getInstance("JKS");

            logger.info("Loading key store: " + keyStoreName);
            logger.info("Loading trust store: " + keyStoreName);
                keystore.load(jksKeyStoreInputStream, keyStorePassword.toCharArray());
            trustManagerKeyStore.load(jksTrustStoreInputStream, keyStorePassword.toCharArray());
            java.security.cert.Certificate[] certs = trustManagerKeyStore.getCertificateChain("ca");
            logger.info("Cert chain: " + Arrays.toString(certs));

            logger.info("LOADED CA CERT: " + trustManagerKeyStore.getCertificate("ca"));
            jksKeyStoreInputStream.close();
            jksTrustStoreInputStream.close();

            // get private key
            Key privKey = keystore.getKey(keystoreAliasName, keyStorePassword.toCharArray());
            // Get certificate of public key
            X509Certificate cert = (X509Certificate) keystore.getCertificate(keystoreAliasName);

            TrustManager[] trustManagers;
            try {
                TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustManagerKeyStore);
                trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException(
                            "Unexpected default trust managers:" + Arrays.toString(trustManagers));
                }
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagers, null);
                sslSocketFactory = sslContext.getSocketFactory();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }

            OkHttpClient client = null;
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
            };
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            sslSocketFactory = sslContext.getSocketFactory();
            client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).build();

            // Get AKI
            //GET 2.5.29.14	SubjectKeyIdentifier / 2.5.29.35	AuthorityKeyIdentifier
            String aki_oid = Extension.authorityKeyIdentifier.getId();
            byte[] rawAuthorityKeyIdentifier = cert.getExtensionValue(aki_oid);
            ASN1OctetString akiOc = ASN1OctetString.getInstance(rawAuthorityKeyIdentifier);
            AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.getInstance(akiOc.getOctets());
            byte[] authorityKeyIdentifier = aki.getKeyIdentifier();

            //GET SKI
            String ski_oid = Extension.subjectKeyIdentifier.getId();
            byte[] rawSubjectKeyIdentifier = cert.getExtensionValue(ski_oid);
            ASN1OctetString ski0c = ASN1OctetString.getInstance(rawSubjectKeyIdentifier);
            SubjectKeyIdentifier ski = SubjectKeyIdentifier.getInstance(ski0c.getOctets());
            byte[] subjectKeyIdentifier = ski.getKeyIdentifier();

            String aki_result = beatifyHex(encodeHexString(authorityKeyIdentifier).toUpperCase());
            String ski_result = beatifyHex(encodeHexString(subjectKeyIdentifier).toUpperCase());

            String connectorUUID = ski_result + "keyid:" + aki_result.substring(0, aki_result.length() - 1);

            logger.info("ConnectorUUID: " + connectorUUID);
            logger.info("Retrieving Dynamic Attribute Token...");


            // create signed JWT (JWS)
            // Create expiry date one day (86400 seconds) from now
            Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
            JwtBuilder jwtb =
                    Jwts.builder()
                            .setIssuer(connectorUUID)
                            .setSubject(connectorUUID)
                            .claim("@context", "https://w3id.org/idsa/contexts/context.jsonld")
                            .claim("@type", "ids:DatRequestToken")
                            .setExpiration(expiryDate)
                            .setIssuedAt(Date.from(Instant.now()))
                            .setAudience(targetAudience)
                            .setNotBefore(Date.from(Instant.now()));

            String jws = jwtb.signWith(SignatureAlgorithm.RS256, privKey).compact();
            logger.info("Request token: " + jws);

            // build form body to embed client assertion into post request
            RequestBody formBody =
                    new FormBody.Builder()
                            .add("grant_type", "client_credentials")
                            .add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                            .add("client_assertion", jws)
                            .add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
                            .build();

            Request request = new Request.Builder().url(dapsUrl).post(formBody).build();
            jwtResponse = client.newCall(request).execute();
            if (!jwtResponse.isSuccessful()) {
                throw new IOException("Unexpected code " + jwtResponse);
            }
            var responseBody = jwtResponse.body();
            if (responseBody == null) {
                throw new Exception("JWT response is null.");
            }
            var jwtString = responseBody.string();
            logger.info("Response body of token request:\n{}", jwtString);
            ObjectNode node = new ObjectMapper().readValue(jwtString, ObjectNode.class);

            if (node.has("access_token")) {
                token = node.get("access_token").asText();
                logger.debug("access_token: {}",  token.toString());
            } else {
            	logger.info("jwtResponse: {}",  jwtResponse.toString());
            }
        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | CertificateException
                | UnrecoverableKeyException e) {
            logger.error("Cannot acquire token:", e);
        } catch (IOException e) {
            logger.error("Error retrieving token:", e);
        } catch (Exception e) {
            logger.error("Something else went wrong:", e);
        } finally {
			jwtResponse.close();
		}
        return token;
    }

    /* //NOT USED at the moment!
    public Map<String, Object> verifyJWT(
            String dynamicAttributeToken,
            String dapsUrl) throws Exception {
        if (sslSocketFactory == null) {
            throw new JwtException("SSLSocketFactory is null, acquireToken() must be called first!");
        }
        try {
            // The HttpsJwks retrieves and caches keys from a the given HTTPS JWKS endpoint.
            // Because it retains the JWKs after fetching them, it can and should be reused
            // to improve efficiency by reducing the number of outbound calls the the endpoint.
            HttpsJwks httpsJkws = new HttpsJwks(dapsUrl + "/.well-known/jwks.json");
            Get getInstance = new Get();
            getInstance.setSslSocketFactory(sslSocketFactory);
            httpsJkws.setSimpleHttpGet(getInstance);
            // The HttpsJwksVerificationKeyResolver uses JWKs obtained from the HttpsJwks and will select
            // the most appropriate one to use for verification based on the Key ID and other factors
            // provided in the header of the JWS/JWT.
            HttpsJwksVerificationKeyResolver httpsJwksKeyResolver =
                    new HttpsJwksVerificationKeyResolver(httpsJkws);
            // Use JwtReceiverBuilder to construct an appropriate JwtReceiver, which will
            // be used to validate and process the JWT.
            // The specific validation requirements for a JWT are context dependent, however,
            // it typically advisable to require a (reasonable) expiration time, a trusted issuer, and
            // and audience that identifies your system as the intended recipient.
            // If the JWT is encrypted too, you need only provide a decryption key or
            // decryption key resolver to the builder.
            JwtReceiver jwtReceiver =
                    new JwtReceiverBuilder()
                            .setRequireExpirationTime() // the JWT must have an expiration time
                            .setAllowedClockSkewInSeconds(
                                    30) // allow some leeway in validating time based claims to account for clock skew
                            .setRequireSubject() // the JWT must have a subject claim
                            .setExpectedIssuer(
                                    "https://daps.aisec.fraunhofer.de") // whom the JWT needs to have been issued by
                            //FIXME: Hardcoded two v1 and v2 values. Need to add versioning to correctly handle tokens.
                            .setExpectedAudience(true, "IDS_Connector", "idsc:IDS_CONNECTORS_ALL") // to whom the JWT is intended for
                            .setVerificationKeyResolver(httpsJwksKeyResolver)
                            .setJwsAlgorithmConstraints( // only allow the expected signature algorithm(s) in the
                                    // given context
                                    new org.jose4j.jwa.AlgorithmConstraints(
                                            org.jose4j.jwa.AlgorithmConstraints.ConstraintType
                                                    .WHITELIST, // which is only RS256 here
                                            AlgorithmIdentifiers.RSA_USING_SHA256))
                            .build(); // create the JwtReceiver instance
            LOG.info("Verifying JWT...");
            //  Validate the JWT and process it to the Claims
            JwtClaims jwtClaims = jwtReceiver.processToClaims(dynamicAttributeToken);
            LOG.info("JWT validation succeeded! " + jwtClaims);
            return jwtClaims.getClaimsMap();
        } catch (InvalidJwtException e) {
            // InvalidJwtException will be thrown, if the JWT failed processing or validation in anyway.
            // Hopefully with meaningful explanations(s) about what went wrong.
            LOG.warn("Invalid JWT!", e);
            // Programmatic access to (some) specific reasons for JWT invalidity is also possible
            // should you want different error handling behavior for certain conditions.
            // Whether or not the JWT has expired being one common reason for invalidity
            if (e.hasExpired()) {
                try {
                    LOG.warn("JWT expired at " + e.getJwtContext().getJwtClaims().getExpirationTime());
                } catch (MalformedClaimException e1) {
                    LOG.error("Malformed claim encountered", e1);
                }
            }
            // Or maybe the audience was invalid
            if (e.hasErrorCode(ErrorCodes.AUDIENCE_INVALID)) {
                try {
                    LOG.warn("JWT had wrong audience: " + e.getJwtContext().getJwtClaims().getAudience());
                } catch (MalformedClaimException e1) {
                    LOG.error("Malformed claim encountered", e1);
                }
            }
            throw e;
        }
    }*/


    /***
     * Split string ever len chars and return string array
     * @param src
     * @param len
     * @return
     */
    public static String[] split(String src, int len) {
        String[] result = new String[(int)Math.ceil((double)src.length()/(double)len)];
        for (int i=0; i<result.length; i++)
            result[i] = src.substring(i*len, Math.min(src.length(), (i+1)*len));
        return result;
    }

    /***
     * Beautyfies Hex strings and will generate a result later used to create the client id (XX:YY:ZZ)
     * @param hexString HexString to be beautified
     * @return beautifiedHex result
     */
    public String beatifyHex(String hexString) {
        String[] splitString = split(hexString, 2);
        StringBuffer sb = new StringBuffer();
        for(int i =0; i < splitString.length; i++) {
            sb.append(splitString[i]);
            sb.append(":");
        }
        return sb.toString();
    }

    /**
     * Convert byte array to hex without any dependencies to libraries.
     * @param num
     * @return
     */
    public String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    /**
     * Encode a byte array to an hex string
     * @param byteArray
     * @return
     */
    public String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }


    @Override
    public boolean validateToken(String tokenValue) {
        boolean isValid = false;

        logger.debug("Get properties");

        try {
            // Set up a JWT processor to parse the tokens and then check their signature
            // and validity time window (bounded by the "iat", "nbf" and "exp" claims)
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<SecurityContext>();

            // The public RSA keys to validate the signatures will be sourced from the
            // OAuth 2.0 server's JWK set, published at a well-known URL. The RemoteJWKSet
            // object caches the retrieved keys to speed up subsequent look-ups and can
            // also gracefully handle key-rollover
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<SecurityContext>(
                    new URL(dapsJWKSUrl));

            // Load JWK set from URL
            JWKSet publicKeys = null;
                publicKeys = JWKSet.load(new URL(dapsJWKSUrl));
            RSAKey key = (RSAKey) publicKeys.getKeyByKeyId("default");

            // The expected JWS algorithm of the access tokens (agreed out-of-band)
            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

            // Configure the JWT processor with a key selector to feed matching public
            // RSA keys sourced from the JWK set URL
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<SecurityContext>(
                    expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);

            // Validate signature
            String exponentB64u = key.getPublicExponent().toString();
            String modulusB64u = key.getModulus().toString();

            // Build the public key from modulus and exponent
            PublicKey publicKey = DapsServiceImpl.getPublicKey(modulusB64u, exponentB64u);

            // print key as PEM (base64 and headers)
            String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "\n" + "-----END PUBLIC KEY-----";

            logger.debug("publicKeyPEM: {}", publicKeyPEM);

            //get signed data and signature from JWT
            String signedData = tokenValue.substring(0, tokenValue.lastIndexOf("."));
            String signatureB64u = tokenValue.substring(tokenValue.lastIndexOf(".") + 1, tokenValue.length());
            byte signature[] = Base64.getUrlDecoder().decode(signatureB64u);

            //verify Signature
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            boolean v = sig.verify(signature);
            logger.debug("result_validation_signature = ",  v);

            if (v == false) {
                isValid = false;
            } else {
                // Process the token
                SecurityContext ctx = null; // optional context parameter, not required here
                JWTClaimsSet claimsSet = jwtProcessor.process(tokenValue, ctx);

                logger.debug("claimsSet = ",  claimsSet.toJSONObject());

                isValid = true;
            }

        } catch (Exception e) {
        	logger.error(e.getMessage());
        }

        return isValid;
    }

}