package it.eng.idsa.businesslogic.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import it.eng.idsa.businesslogic.audit.TrueConnectorAuditableInterceptor;

@Configuration
public class TrueConnectorWebMvcConfigurer implements WebMvcConfigurer {

	@Autowired
	private TrueConnectorAuditableInterceptor trueConnectorInterceptor;
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trueConnectorInterceptor);
    }
}
