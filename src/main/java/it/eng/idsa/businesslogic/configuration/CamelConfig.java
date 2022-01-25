package it.eng.idsa.businesslogic.configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.camel.model.Constants;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Bean
    public Unmarshaller unmarshaller() {
        try {
            final var jaxb = JAXBContext.newInstance(Constants.JAXB_CONTEXT_PACKAGES);
            return jaxb.createUnmarshaller();
        } catch (JAXBException e) {
            throw new BeanCreationException("Failed to create Unmarshaller.", e);
        }
    }
}
