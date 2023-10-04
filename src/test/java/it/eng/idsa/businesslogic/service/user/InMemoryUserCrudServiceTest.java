package it.eng.idsa.businesslogic.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import it.eng.idsa.businesslogic.util.TrueConnectorConstants;

public class InMemoryUserCrudServiceTest {

	@InjectMocks
	private InMemoryUserCrudService userService;
	@Mock
	private HttpServletRequest request;
	@Mock
	private LoginAttemptService loginAttemptService;
	@Mock
	private PasswordEncoder encoder;
	
	private User user;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		user = new User(UUID.randomUUID().toString(), "testUsername", "testPassword", TrueConnectorConstants.API_USER_ROLE);
	}
	
	@Test
	public void loadUserByUsername() {
		userService.save(user);
		UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
		assertNotNull(userDetails);
	}
	
	@Test
	public void loadUserByUsername_NotFound() {
		assertThrows(UsernameNotFoundException.class,
	                () -> userService.loadUserByUsername(user.getUsername()));
	}
	
	@Test
	public void saveUser() {
		User savedUser = userService.save(user);
		assertNull(savedUser);
	}
	
	@Test
	public void findUser() {
		userService.save(user);
		Optional<User> findUser = userService.find(user.getId());
		assertNotNull(findUser.get());
	}
	
	@Test
	public void findUser_NotFound() {
		userService.save(user);
		Optional<User> findUser = userService.find(UUID.randomUUID().toString());
		assertFalse(findUser.isPresent());
	}
	
	@Test
	public void findUserByUsername() {
		userService.save(user);
		
		when(loginAttemptService.isBlocked(any(String.class))).thenReturn(false);
		
		UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
		assertNotNull(userDetails);
	}
	
	@Test
	public void findUserByUsernameAndPassword() {
		userService.save(user);
		
		when(loginAttemptService.isBlocked(any(String.class))).thenReturn(false);
		when(encoder.matches(any(String.class), any(String.class))).thenReturn(true);
		
		Optional<User> findUser = userService.findByUsernameAndPassword(user.getUsername(), user.getPassword());
		assertNotNull(findUser.get());
	}
	
	@Test
	public void findUserByUsernameAndPassword_wrong_password() {
		userService.save(user);
		
		when(loginAttemptService.isBlocked(any(String.class))).thenReturn(false);
		when(encoder.matches(any(String.class), any(String.class))).thenReturn(false);
		
		Optional<User> findUser = userService.findByUsernameAndPassword(user.getUsername(), user.getPassword());
		assertFalse(findUser.isPresent());
	}
	
	@Test
	public void findUserBlocked() {
		when(request.getHeader("X-Forwarded-For")).thenReturn("localhost");
		when(loginAttemptService.isBlocked(any(String.class))).thenReturn(true);
		
		 RuntimeException exception = assertThrows(RuntimeException.class,
	                () -> userService.findByUsernameAndPassword(user.getUsername(), user.getPassword()));
		 
		 assertEquals(exception.getMessage(), "blocked");
	}
}
