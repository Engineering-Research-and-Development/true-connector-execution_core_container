package it.eng.idsa.businesslogic.web.rest;

import it.eng.idsa.businesslogic.service.HashFileService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HashResourceTest {

	@InjectMocks
	private HashResource hashResource;

	@Mock
	private HashFileService hashService;
	@Mock
	private PasswordValidatorService passwordValidatorService;
	@Mock
	private PasswordEncoder passwordEncoder;

	private String forHashing = "STRING TO HASH";
	private String hashedValue = "HASHED STRING";
	private final String password = "Password";

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void hashTest() throws Exception {
		when(hashService.getContent(forHashing)).thenReturn(hashedValue);
		String hashed = hashResource.getPayload(forHashing);

		assertEquals(hashedValue, hashed);
		verify(hashService).getContent(forHashing);
	}

	@Test
	void getvalidPasswordTest() {
		when(passwordValidatorService.validate(password)).thenReturn(Collections.emptyList());
		when(passwordEncoder.encode(password)).thenReturn("Encoded Password");

		var responseEntity = hashResource.getPassword(password);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void getInvalidPasswordTest() {
		when(passwordValidatorService.validate(password)).thenReturn(List.of("Error"));
		var responseEntity = hashResource.getPassword(password);

		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}
}
