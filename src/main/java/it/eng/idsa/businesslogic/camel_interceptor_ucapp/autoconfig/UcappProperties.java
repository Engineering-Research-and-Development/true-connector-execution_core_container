package it.eng.idsa.businesslogic.camel_interceptor_ucapp.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Robin Brandstaedter <Robin.Brandstaedter@iese.fraunhofer.de>
 *
 */
@ConfigurationProperties(prefix = "spring.ids.ucapp")
public class UcappProperties {

	private String baseUrl;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	
}
