package it.eng.idsa.businesslogic.service.impl;

import java.util.Collections;
import java.util.List;

import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.configuration.PasswordConfig;

@Service
public class PasswordValidatorService {
	private static final Logger logger = LoggerFactory.getLogger(PasswordValidatorService.class);

	private final PasswordConfig passwordConfig;

	public PasswordValidatorService(PasswordConfig passwordConfig) {
		this.passwordConfig = passwordConfig;
	}

	public List<String> validate(String password) {
		if (password == null) {
			logger.error("Password is null");
			return List.of("Password can not be null");
		}
		PasswordValidator passwordValidator = new PasswordValidator(passwordConfig.getRules());
		RuleResult ruleResult = passwordValidator.validate(new PasswordData(password));

		if (ruleResult.isValid()) {
			logger.info("Password is valid");
			return Collections.emptyList();
		}

		List<String> errors = passwordValidator.getMessages(ruleResult);
		logger.warn("Invalid Password: {}", errors);
		return errors;
	}
}
