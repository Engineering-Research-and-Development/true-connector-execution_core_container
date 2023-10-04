package it.eng.idsa.businesslogic.web.rest;

import it.eng.idsa.businesslogic.service.impl.PasswordValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PasswordControllerTest {

	@InjectMocks
	private PasswordController passwordController;

	@Mock
	private PasswordValidatorService passwordValidatorService;
	@Mock
	private PasswordEncoder passwordEncoder;

	private final String password = "Password";

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void getvalidPasswordTest() {
		when(passwordValidatorService.validate(password)).thenReturn(Collections.emptyList());
		when(passwordEncoder.encode(password)).thenReturn("Encoded Password");

		var responseEntity = passwordController.getPassword(password);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void getInvalidPasswordTest() {
		when(passwordValidatorService.validate(password)).thenReturn(List.of("Error"));
		var responseEntity = passwordController.getPassword(password);

		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}
}
