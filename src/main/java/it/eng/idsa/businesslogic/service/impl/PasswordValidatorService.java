package it.eng.idsa.businesslogic.service.impl;

import it.eng.idsa.businesslogic.configuration.PasswordConfig;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PasswordValidatorService {
	private static final Logger logger = LoggerFactory.getLogger(PasswordValidatorService.class);

	private final PasswordConfig passwordConfig;

	public PasswordValidatorService(PasswordConfig passwordConfig) {
		this.passwordConfig = passwordConfig;
	}

	public List<String> validate(String password) {
		List<Rule> rules = getRules();

		PasswordValidator passwordValidator = new PasswordValidator(rules);
		RuleResult ruleResult = passwordValidator.validate(new PasswordData(password));

		if (ruleResult.isValid()) {
			logger.info("Password is valid");
			return Collections.emptyList();
		}

		List<String> errors = passwordValidator.getMessages(ruleResult);
		logger.warn("Invalid Password: {}", errors);
		return errors;
	}

	public List<Rule> getRules() {
		return passwordConfig.getRules();
	}

}
