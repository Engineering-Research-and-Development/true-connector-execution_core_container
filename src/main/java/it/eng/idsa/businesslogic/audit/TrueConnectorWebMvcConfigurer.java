package it.eng.idsa.businesslogic.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TrueConnectorWebMvcConfigurer implements WebMvcConfigurer {

	@Autowired
	private TrueConnectorAuditableInterceptor trueConnectorInterceptor;
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trueConnectorInterceptor);
    }
}
