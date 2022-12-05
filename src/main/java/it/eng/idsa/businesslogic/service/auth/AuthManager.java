package it.eng.idsa.businesslogic.service.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.user.InMemoryUserCrudService;

@Component
public class AuthManager implements AuthenticationManager {

	private PasswordEncoder passwordEncoder;
	private InMemoryUserCrudService userDetailsService;
	
	public AuthManager(PasswordEncoder passwordEncoder, InMemoryUserCrudService userDetailsService) {
		super();
		this.passwordEncoder = passwordEncoder;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		final UserDetails userDetail = userDetailsService.loadCamelUserByUsername(authentication.getName());
		
		if (!passwordEncoder.matches(authentication.getCredentials().toString(), userDetail.getPassword())) {
			throw new BadCredentialsException("Wrong password");
		}
		return new UsernamePasswordAuthenticationToken(userDetail.getUsername(), userDetail.getPassword(),
				userDetail.getAuthorities());
	}
}
