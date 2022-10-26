package it.eng.idsa.businesslogic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
    public void configure(WebSecurity web) throws Exception {
		// allow Swagger UI to be displayed without asking credentials in browser
        web.ignoring().antMatchers("/swagger-ui.html", "/swagger-ui/**");
    }
	 
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.cors()
			.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.authorizeRequests()
			.antMatchers("/").permitAll()
			.antMatchers("/about/**").permitAll()
			.antMatchers("/api/**").authenticated()
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
		entryPoint.setRealmName("admin realm");
		return entryPoint;
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}