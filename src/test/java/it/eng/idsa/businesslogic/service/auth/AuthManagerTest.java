package it.eng.idsa.businesslogic.service.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import it.eng.idsa.businesslogic.service.user.InMemoryUserCrudService;

public class AuthManagerTest {

	@InjectMocks
	private AuthManager authManager;
	
	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private InMemoryUserCrudService userDetailsService;
	@Mock
	private Authentication authentication;
	@Mock
	private UserDetails userDetails;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void authenticateUserSuccess() {
		when(authentication.getName()).thenReturn("username");
		when(authentication.getCredentials()).thenReturn("password");
		when(userDetails.getPassword()).thenReturn("encodedPassword");
		when(userDetailsService.loadCamelUserByUsername(authentication.getName())).thenReturn(userDetails);
		when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
		
		Authentication auth = authManager.authenticate(authentication);
		assertNotNull(auth);
	}
	
	@Test
	public void authenticateUserFailed() {
		when(authentication.getName()).thenReturn("username");
		when(authentication.getCredentials()).thenReturn("password");
		when(userDetails.getPassword()).thenReturn("encodedPassword");
		when(userDetailsService.loadCamelUserByUsername(authentication.getName())).thenReturn(userDetails);
		when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(false);
		
		assertThrows(BadCredentialsException.class,
                () -> authManager.authenticate(authentication));
	}
}
