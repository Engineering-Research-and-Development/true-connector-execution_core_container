package it.eng.idsa.businesslogic.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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

	/**
	 * Username defined in application.properties.
	 */
	@Value("${spring.security.user.name}")
	private String username;

	/**
	 * Password defined in application.properties.
	 */
	@Value("${spring.security.user.password}")
	private String password;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.authorizeRequests()
			.antMatchers("/").anonymous()
			.antMatchers("/about/**").anonymous()
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
	public UserDetailsService userDetailsService() {
		final var manager = new InMemoryUserDetailsManager();
		manager.createUser(User.withUsername(username)
				.password(encoder().encode(password))
				.authorities("ADMIN")
				.build());
		return manager;
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}