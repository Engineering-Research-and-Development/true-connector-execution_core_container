package it.eng.idsa.businesslogic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import de.fhg.aisec.ids.camel.idscp2.processors.IdsMessageTypeExtractionProcessor;

@Configuration
public class IdsMessageBean {

	@Bean
	public IdsMessageTypeExtractionProcessor typeExtractionProcessor() {
		IdsMessageTypeExtractionProcessor typeExtractionProcessor = new IdsMessageTypeExtractionProcessor();
		return typeExtractionProcessor;
        
    } 	
}
