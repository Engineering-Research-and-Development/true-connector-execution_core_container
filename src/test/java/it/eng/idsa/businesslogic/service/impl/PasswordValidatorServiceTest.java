package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.eng.idsa.businesslogic.configuration.PasswordConfig;

class PasswordValidatorServiceTest {

	private PasswordConfig passwordConfig;

	private PasswordValidatorService passwordValidatorService;

	@BeforeEach
	public void init() {
		passwordConfig = new PasswordConfig();
		passwordConfig.setMinLength(8);
		passwordConfig.setMaxLength(16);
		passwordConfig.setMinUpperCase(1);
		passwordConfig.setMinLowerCase(1);
		passwordConfig.setMinDigit(1);
		passwordConfig.setMinSpecial(1);
		passwordConfig.configure();

		passwordValidatorService = new PasswordValidatorService(passwordConfig);
	}

	@Test
	void validateTest() {
		assertTrue(passwordValidatorService.validate("Abc*5678").isEmpty());
	}

	@Test
	void validatePasswordTooShortTest() {
		List<String> list = passwordValidatorService.validate("Ts*5");
		assertEquals(1, list.size());

		String expected = "Password must be " + passwordConfig.getMinLength() + " or more characters in length.";
		assertEquals(expected, list.get(0));
	}

	@Test
	void validatePasswordTooLongTest() {
		List<String> list = passwordValidatorService.validate("TooLongPassword*17");
		assertEquals(1, list.size());

		String expected = "Password must be no more than " + passwordConfig.getMaxLength() + " characters in length.";
		assertEquals(expected, list.get(0));
	}

	@Test
	void validatePasswordWithWhitespaceTest() {
		List<String> list = passwordValidatorService.validate("W1th White$pace");
		assertEquals(1, list.size());

		String expected = "Password contains a whitespace character.";
		assertEquals(expected, list.get(0));
	}

	@Test
	void validatePasswordNotEnoughUpperCaseTest() {
		List<String> list = passwordValidatorService.validate("nouppercase*14");
		assertEquals(1, list.size());

		String expected = "Password must contain " + passwordConfig.getMinUpperCase() + " or more uppercase characters.";
		assertEquals(expected, list.get(0));
	}

	@Test
	void validatePasswordNotEnoughLowerCaseTest() {
		List<String> list = passwordValidatorService.validate("NOLOWERCASE*14");
		assertEquals(1, list.size());

		String expected = "Password must contain " + passwordConfig.getMinLowerCase() + " or more lowercase characters.";
		assertEquals(expected, list.get(0));
	}

	@Test
	void validatePasswordNotEnoughDigitTest() {
		List<String> list = passwordValidatorService.validate("WithoutDigit!");
		assertEquals(1, list.size());

		String expected = "Password must contain " + passwordConfig.getMinDigit() + " or more digit characters.";
		assertEquals(expected, list.get(0));
	}

	@Test
	void validatePasswordNotEnoughSpecialTest() {
		List<String> list = passwordValidatorService.validate("W1thoutSpecial");
		assertEquals(1, list.size());

		String expected = "Password must contain " + passwordConfig.getMinSpecial() + " or more special characters.";
		assertEquals(expected, list.get(0));
	}
	
	@Test
	void validatePasswordNullTest() {
		List<String> list = passwordValidatorService.validate(null);
		assertEquals(1, list.size());

		assertEquals("Password can not be null", list.get(0));
	}

}