package it.eng.idsa.businesslogic.configuration;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import it.eng.idsa.businesslogic.service.user.TokenAuthenticationFilter;
import it.eng.idsa.businesslogic.service.user.TokenAuthenticationProvider;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	 private static final RequestMatcher PROTECTED_URLS = new OrRequestMatcher(
			    new AntPathRequestMatcher("/api/**")
			  );

	@Autowired
	TokenAuthenticationProvider provider;

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(provider);
		auth.authenticationEventPublisher(defaultAuthenticationEventPublisher());
	}
	
	@Bean
	public DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(){
	    return new DefaultAuthenticationEventPublisher();
	}
	 
	@Override
	  protected void configure(final HttpSecurity http) throws Exception {
		//@formatter:off
	    http
	      .cors()
	      .and()
	      .sessionManagement()
	      .sessionCreationPolicy(STATELESS)
	      .and()
	      .exceptionHandling()
	      // this entry point handles when you request a protected page and you are not yet
	      // authenticated
	      .defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), PROTECTED_URLS)
	      .and()
	      .authenticationProvider(provider)
	      .addFilterBefore(restAuthenticationFilter(), AnonymousAuthenticationFilter.class)
	      .authorizeRequests()
	      .requestMatchers(PROTECTED_URLS)
	      .authenticated()
	      .and()
	      .csrf().disable()
	      .formLogin().disable()
	      .httpBasic().disable()
	      .logout().disable();
	  //@formatter:on
	  }

	@Bean
	AuthenticationEntryPoint forbiddenEntryPoint() {
		return new HttpStatusEntryPoint(FORBIDDEN);
	}

	@Bean
	TokenAuthenticationFilter restAuthenticationFilter() throws Exception {
		final TokenAuthenticationFilter filter = new TokenAuthenticationFilter(PROTECTED_URLS);
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
	/*
	*/
}