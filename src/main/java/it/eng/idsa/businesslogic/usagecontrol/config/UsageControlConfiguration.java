package it.eng.idsa.businesslogic.usagecontrol.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.usagecontrol.autoconfig.UcappProperties;
import it.eng.idsa.businesslogic.usagecontrol.service.UcRestCallService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Robin Brandstaedter <Robin.Brandstaedter@iese.fraunhofer.de>
 *
 */
@Configuration
@EnableConfigurationProperties({ UcappProperties.class})
public class UsageControlConfiguration {
	
	
	@Bean
	public UcRestCallService ucRestCallService(UcappProperties ucappProperties) {
		return new Retrofit.Builder().baseUrl(ucappProperties.getBaseUrl()).addConverterFactory(GsonConverterFactory.create()).build().create(UcRestCallService.class);
	}

}
