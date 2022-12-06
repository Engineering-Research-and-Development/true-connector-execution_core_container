package it.eng.idsa.businesslogic.service.impl;

import it.eng.idsa.businesslogic.configuration.PasswordConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.passay.Rule;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
class PasswordValidatorServiceTest {

	@Mock
	private PasswordConfig passwordConfig;

	@Spy
	@InjectMocks
	private PasswordValidatorService passwordValidatorService;

	@BeforeEach
	public void init() {
		passwordConfig = new PasswordConfig();
		passwordConfig.setMinLength(8);
		passwordConfig.setMaxLength(16);
		passwordConfig.setEnabledWhitespaceRule(true);
		passwordConfig.setMinUpperCase(1);
		passwordConfig.setMinLowerCase(1);
		passwordConfig.setMinDigit(1);
		passwordConfig.setMinSpecial(1);
		passwordConfig.configure();

		List<Rule> rules = passwordConfig.getRules();

		when(passwordValidatorService.getRules()).thenReturn(rules);
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

}