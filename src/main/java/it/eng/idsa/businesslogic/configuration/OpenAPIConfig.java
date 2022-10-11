package it.eng.idsa.businesslogic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
public class OpenAPIConfig {
	final String securitySchemeName = "bearerAuth";
	
	@Bean
	public OpenAPI api() {
		return new OpenAPI()
				.info(apiDetails())
				.components(new Components().addSecuritySchemes("basicAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")))
				.addSecurityItem(new SecurityRequirement().addList("basicAuth"));
	}

	private Info apiDetails() {
		return new Info()
				.description("Used for creating or updating the self description document")
				.title("Self description API");
	}
	
}
