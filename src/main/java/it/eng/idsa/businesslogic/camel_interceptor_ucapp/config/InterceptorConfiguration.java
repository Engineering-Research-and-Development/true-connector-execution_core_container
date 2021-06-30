package it.eng.idsa.businesslogic.camel_interceptor_ucapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.camel_interceptor_ucapp.autoconfig.UcappProperties;
import it.eng.idsa.businesslogic.camel_interceptor_ucapp.service.UcRestCallService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Robin Brandstaedter <Robin.Brandstaedter@iese.fraunhofer.de>
 *
 */
@Configuration
@EnableConfigurationProperties({ UcappProperties.class})
public class InterceptorConfiguration {
	
	
	@Bean
	public UcRestCallService ucRestCallService(UcappProperties ucappProperties) {
		return new Retrofit.Builder().baseUrl(ucappProperties.getBaseUrl()).addConverterFactory(GsonConverterFactory.create()).build().create(UcRestCallService.class);
	}

}
