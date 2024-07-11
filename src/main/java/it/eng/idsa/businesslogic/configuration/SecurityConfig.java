package it.eng.idsa.businesslogic.configuration;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
	
	@Value("${application.cors.allowed.origins:}")
	private String allowedOrigins;

	@Value("${application.cors.allowed.methods:}")
	private String allowedMethods;

	@Value("${application.cors.allowed.headers:}")
	private String allowedHeaders;
	
	@Autowired
	private UserDetailsService inMemoryUserCrudService;

	@Override
	public void configure(WebSecurity web) throws Exception {
		// allow Swagger UI to be displayed without asking credentials in browser
		web.ignoring().antMatchers("/swagger-ui.html", "/swagger-ui/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and()
		.csrf().disable()
		.httpBasic().authenticationEntryPoint(authenticationEntryPoint()).and()
		.userDetailsService(inMemoryUserCrudService).sessionManagement()
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
		.antMatchers("/about/**").permitAll().antMatchers("/error").permitAll().antMatchers("/").permitAll()
		.antMatchers("/api/**").hasRole(TrueConnectorConstants.API_USER_ROLE).anyRequest().authenticated();

		http.headers().xssProtection();
	}

	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		final var entryPoint = new BasicAuthenticationEntryPoint();
		entryPoint.setRealmName("api realm");
		return entryPoint;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		if (StringUtils.isBlank(allowedOrigins)) {
			configuration.addAllowedOrigin("*");
		} else {
			configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
		}

		if (StringUtils.isBlank(allowedMethods)) {
			configuration.addAllowedMethod("*");
		} else {
			configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
		}

		if (StringUtils.isBlank(allowedHeaders)) {
			configuration.addAllowedHeader("*");
		} else {
			configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
		}

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}