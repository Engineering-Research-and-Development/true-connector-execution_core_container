package it.eng.idsa.businesslogic.configuration;
	
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import it.eng.idsa.businesslogic.util.TrueConnectorConstants;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
@EnableWebSecurity
@EnableScheduling
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private UserDetailsService inMemoryUserCrudService;
	
	@Override
    public void configure(WebSecurity web) throws Exception {
		// allow Swagger UI to be displayed without asking credentials in browser
        web.ignoring().antMatchers("/swagger-ui.html", "/swagger-ui/**");
    }
	 
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.userDetailsService(inMemoryUserCrudService)
			.cors()
			.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.authorizeRequests()
			.antMatchers("/about/**").permitAll()
			.antMatchers("/error").permitAll()
			.antMatchers("/").permitAll()
			.antMatchers("/api/**").hasRole(TrueConnectorConstants.API_USER_ROLE)
			.anyRequest().authenticated()
			.and()
			.csrf().disable()
			.httpBasic()
			.authenticationEntryPoint(authenticationEntryPoint());
		http.headers().xssProtection();
	}

	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		final var entryPoint = new BasicAuthenticationEntryPoint();
		entryPoint.setRealmName("api realm");
		return entryPoint;
	}
}