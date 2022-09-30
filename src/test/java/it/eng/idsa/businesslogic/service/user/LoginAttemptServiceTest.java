package it.eng.idsa.businesslogic.service.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginAttemptServiceTest {

	private LoginAttemptService loginService;
	
	private String ipAddress = "test";

	private int lockDuration = 5;
	private String lockUnit = "MINUTES";
	private int maxAttempts = 2;
	
	@BeforeEach
	public void setup() {
		loginService = new LoginAttemptService(lockDuration, lockUnit, maxAttempts);
	}
	
	@Test
	public void loginSucceeded() {
		assertDoesNotThrow(() -> loginService.loginSucceeded(ipAddress));
	}
	
	@Test
	public void loginFailed() {
		assertDoesNotThrow(() -> loginService.loginFailed(ipAddress));
	}
	
	@Test
	public void userBlocked() {
		loginService.loginFailed(ipAddress);
		assertFalse(loginService.isBlocked(ipAddress));
		loginService.loginFailed(ipAddress);
		loginService.loginFailed(ipAddress);
		assertTrue(loginService.isBlocked(ipAddress));
	}
}
