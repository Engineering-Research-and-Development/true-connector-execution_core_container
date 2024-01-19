package it.eng.idsa.businesslogic.service.user;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:users.properties")
public class UserConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(UserConfiguration.class);

	private final Map<String, String> userCredentials = new HashMap<String, String>();

	public UserConfiguration(Environment env) {
		String usersList = env.getProperty("users.list");
		if (usersList != null) {
			for (String user : usersList.split(",")) {
				String username = user.trim();
				String password = env.getProperty(username + ".password");
				if (StringUtils.isEmpty(password)) {
					logger.error("Password for user: " + user + " is blank, user didn't loaded!");
				} else {
					userCredentials.put(username, password);
				}
			}
		} else {
			logger.error("User.properties file empty, please create users!");
			throw new RuntimeException("User.properties file empty, please create users!");
		}
	}

	public String getPasswordForUser(String user) {
		return userCredentials.get(user);
	}

	public Map<String, String> getUserCredentials() {
		return userCredentials;
	}
}
	