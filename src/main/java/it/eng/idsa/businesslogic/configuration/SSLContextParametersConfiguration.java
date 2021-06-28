package it.eng.idsa.businesslogic.configuration;


import java.nio.file.FileSystems;

import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextClientParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SSLContextParametersConfiguration {
	
	@Value("${application.targetDirectory}")
	private String targetDirectory;
	
	@Value("${application.keyStoreName}")
	private String keyStoreName;
	
	@Value("${application.keyStorePassword}")
	private String keyStorePassword;
	
	@Value("${application.trustStoreName:}")
	private String trustStoreName;
	
	@Value("${application.trustStorePassword}")
	private String trustStorePassword;
	
	@Bean
	@ConditionalOnProperty(
		    value="${application.idscp2.isEnabled}", 
		    havingValue = "true", 
		    matchIfMissing = true)
	public SSLContextParameters sslContext() {
		final KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource(targetDirectory + FileSystems.getDefault().getSeparator() + keyStoreName);
		ksp.setPassword(keyStorePassword);
		// ksp.setType("RSA");

		final KeyManagersParameters kmp = new KeyManagersParameters();
		kmp.setKeyStore(ksp);
		kmp.setKeyPassword(keyStorePassword);

		final SSLContextClientParameters sslContextClientParameters = new SSLContextClientParameters();
		final SSLContextParameters sslContextParameters = new SSLContextParameters();
		sslContextParameters.setClientParameters(sslContextClientParameters);
		sslContextParameters.setKeyManagers(kmp);
		// sslContextParameters.setCertAlias("1");
		// sslContextParameters.setSecureSocketProtocol("TLSv1.3");

		// so that the client trusts the self-signed server certificate

		final KeyStoreParameters trustStoreParams = new KeyStoreParameters();
		trustStoreParams.setResource(targetDirectory + FileSystems.getDefault().getSeparator() + trustStoreName);
		trustStoreParams.setPassword(trustStorePassword);

		final TrustManagersParameters tmp = new TrustManagersParameters();
		tmp.setKeyStore(trustStoreParams);
		sslContextParameters.setTrustManagers(tmp);

		return sslContextParameters;
    } 
	
	
}
