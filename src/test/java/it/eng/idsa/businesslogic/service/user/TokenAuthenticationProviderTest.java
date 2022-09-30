package it.eng.idsa.businesslogic.service.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class TokenAuthenticationProviderTest {

	@InjectMocks
	private TokenAuthenticationProvider provider;
	
	@Mock
	private UserCrudService userService;
	@Mock
	private UsernamePasswordAuthenticationToken authentication;
	
	private String username = "username";
	private Object password = new String("password");

	private Optional<User> userOptional;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		User user = new User(UUID.randomUUID().toString(), username, (String) password);
		userOptional = Optional.of(user);
	}
	
	@Test
	public void retrieveUser() {
		when(authentication.getCredentials()).thenReturn(password);
		when(userService.findByUsernameAndPassword(username, (String)password)).thenReturn(userOptional);
		UserDetails userDetails = provider.retrieveUser(username, authentication);
		assertNotNull(userDetails);
	}
	
	@Test
	public void retrieveUser_not_found() {
		when(authentication.getCredentials()).thenReturn(password);
		when(userService.findByUsernameAndPassword(username, (String)password)).thenReturn(Optional.empty());
		assertThrows(UsernameNotFoundException.class,
				() -> provider.retrieveUser(username, authentication));
	}
}
