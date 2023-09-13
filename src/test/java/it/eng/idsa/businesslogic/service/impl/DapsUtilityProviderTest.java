package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwk.Jwk;

public class DapsUtilityProviderTest {

	@InjectMocks
	private DapsUtilityProvider dapsUtilityProvider;

	@Mock
	private DapsKeystoreProvider keystoreProvider;
	
	private X509Certificate x509Certificate;
	
	@Mock
	private ASN1OctetString asn1OctetString;
	
	private KeyStore keyStore;
	
	@Mock
	private Key mockKey;

	private URL dapsJWKSUrl;
	private String keyStoreName;
	private String keyStorePassword;
	private String keystoreAliasName;

	@Mock
	private X509Certificate mockCertificate;
	
	@Mock
	private Jwk jwk;
	
	
	@BeforeEach
	public void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		MockitoAnnotations.openMocks(this);
		dapsJWKSUrl = new URL("https://dap.aisec.fraunhofer.de");
		keyStoreName = "classpath:ssl-server.jks";    
		keyStorePassword = "changeit";
		keystoreAliasName = "execution-core-container";
		
		ReflectionTestUtils.setField(dapsUtilityProvider, "dapsJWKSUrl", dapsJWKSUrl);
		
		keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new DefaultResourceLoader().getResource(keyStoreName).getInputStream(), keyStorePassword.toCharArray());
		
		x509Certificate = (X509Certificate) keyStore.getCertificate(keystoreAliasName);
		when(keystoreProvider.getCertificate()).thenReturn(mockCertificate);
	}
	
	@Test
	public void testGetDapsV2Jws() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException{
		when(keystoreProvider.getCertificate().getExtensionValue(Extension.authorityKeyIdentifier.getId())).thenReturn(x509Certificate.getExtensionValue(Extension.authorityKeyIdentifier.getId()));
		when(keystoreProvider.getCertificate().getExtensionValue(Extension.subjectKeyIdentifier.getId())).thenReturn(x509Certificate.getExtensionValue(Extension.subjectKeyIdentifier.getId()));
		when(keystoreProvider.getPrivateKey()).thenReturn(keyStore.getKey(keystoreAliasName, keyStorePassword.toCharArray()));
		
		String jws = dapsUtilityProvider.getDapsV2Jws();
		
		assertNotNull(jws);
		
		assertDoesNotThrow(() -> dapsUtilityProvider.getDapsV2Jws());
	}
	
	@Test
	public void testGetPublicKey() {
		when(keystoreProvider.getCertificate().getPublicKey()).thenReturn(x509Certificate.getPublicKey());
		assertNotNull(dapsUtilityProvider.getPublicKey());
	}
	
}
