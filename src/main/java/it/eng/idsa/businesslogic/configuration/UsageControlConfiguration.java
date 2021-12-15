package it.eng.idsa.businesslogic.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.usagecontrol.service.UcRestCallService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class UsageControlConfiguration {
	
	@Bean
	@ConditionalOnExpression("'${application.isEnabledUsageControl}' == 'true'")
	public UcRestCallService ucRestCallService(@Value("${spring.ids.ucapp.baseUrl}") String usageControlBaseUrl) {
		return new Retrofit.Builder()
				.baseUrl(usageControlBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(UcRestCallService.class);
	}

}
