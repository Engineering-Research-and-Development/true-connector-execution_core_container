package it.eng.idsa.businesslogic.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.Rule;
import org.passay.WhitespaceRule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConfigurationProperties(prefix ="application.password.validator")
public class PasswordConfig {
	private List<Rule> rules = new ArrayList<>();

	private int minLength;
	@Min(1)
	private int maxLength;
	private int minUpperCase;
	private int minLowerCase;

	private int minDigit;
	private int minSpecial;

	@PostConstruct
	public void configure() {
		if (minLength > 0) {
			// Rule 1: Password length should be in between 8 and 16 characters
			rules.add(new LengthRule(minLength, maxLength));
		}

		// Rule 2: Whitespace rule always active
		rules.add(new WhitespaceRule());

		if (minUpperCase > 0) {
			// Rule 3.a: At least one Upper-case character
			rules.add(new CharacterRule(EnglishCharacterData.UpperCase, minUpperCase));
		}

		if (minLowerCase > 0) {
			// Rule 3.b: At least one Lower-case character
			rules.add(new CharacterRule(EnglishCharacterData.LowerCase, minLowerCase));
		}

		if (minDigit > 0) {
			// Rule 3.c: At least one digit
			rules.add(new CharacterRule(EnglishCharacterData.Digit, minDigit));
		}

		if (minSpecial > 0) {
			// Rule 3.d: At least one special character
			rules.add(new CharacterRule(EnglishCharacterData.Special, minSpecial));
		}
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMinUpperCase() {
		return minUpperCase;
	}

	public void setMinUpperCase(int minUpperCase) {
		this.minUpperCase = minUpperCase;
	}

	public int getMinLowerCase() {
		return minLowerCase;
	}

	public void setMinLowerCase(int minLowerCase) {
		this.minLowerCase = minLowerCase;
	}

	public int getMinDigit() {
		return minDigit;
	}

	public void setMinDigit(int minDigit) {
		this.minDigit = minDigit;
	}

	public int getMinSpecial() {
		return minSpecial;
	}

	public void setMinSpecial(int minSpecial) {
		this.minSpecial = minSpecial;
	}
	
	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}
