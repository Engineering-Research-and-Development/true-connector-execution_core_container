package it.eng.idsa.businesslogic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
public class OpenAPIConfig {
	
	@Bean
	public OpenAPI api() {
		return new OpenAPI()
				.info(apiDetails());
	}

	private Info apiDetails() {
		return new Info()
				.description("Used for creating or updating the self description document")
				.title("Self description API");
	}
	
}
