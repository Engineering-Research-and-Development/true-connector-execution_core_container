package it.eng.idsa.businesslogic.configuration;

import java.util.List;

import org.apache.camel.component.spring.security.SpringSecurityAccessPolicy;
import org.apache.camel.component.spring.security.SpringSecurityAuthorizationPolicy;
import org.apache.camel.spi.Policy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CamelSecurityConfiguration {

	public static final String CONNECTOR = "CONNECTOR";
	
	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public Policy adminPolicy(AuthenticationManager authenticationManager) {
	    RoleVoter roleVoter = new RoleVoter();
	    SpringSecurityAuthorizationPolicy policy = new SpringSecurityAuthorizationPolicy();
	    policy.setAuthenticationManager(authenticationManager);
	    policy.setAccessDecisionManager(new UnanimousBased(List.of(roleVoter)));
	    policy.setSpringSecurityAccessPolicy(new SpringSecurityAccessPolicy(roleVoter.getRolePrefix() + CONNECTOR));
	    return policy;
	}
}
