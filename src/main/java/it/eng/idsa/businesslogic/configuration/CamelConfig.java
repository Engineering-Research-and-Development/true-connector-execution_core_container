package it.eng.idsa.businesslogic.configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.model.Constants;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextClientParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {
	
	
	private String trustStore;
	private String trustStorePassword;
	

    public CamelConfig(@Value("${camel.component.jetty.keystore}")String keyStore, @Value("${camel.component.jetty.ssl-password}")String keyStorePassword) {
		super();
		this.trustStore = keyStore;
		this.trustStorePassword = keyStorePassword;
	}

	@Bean
    public Unmarshaller unmarshaller() {
        try {
            final var jaxb = JAXBContext.newInstance(Constants.JAXB_CONTEXT_PACKAGES);
            return jaxb.createUnmarshaller();
        } catch (JAXBException e) {
            throw new BeanCreationException("Failed to create Unmarshaller.", e);
        }
    }
    
    @Bean
	public SSLContextParameters camelXMLSSLContext() {
		final SSLContextClientParameters sslContextClientParameters = new SSLContextClientParameters();
		final SSLContextParameters sslContextParameters = new SSLContextParameters();
		sslContextParameters.setClientParameters(sslContextClientParameters);
		
		final KeyStoreParameters trustStoreParams = new KeyStoreParameters();
		trustStoreParams.setResource(trustStore);
		trustStoreParams.setPassword(trustStorePassword);

		final TrustManagersParameters tmp = new TrustManagersParameters();
		tmp.setKeyStore(trustStoreParams);
		sslContextParameters.setTrustManagers(tmp);
		

		return sslContextParameters;
    } 
}
