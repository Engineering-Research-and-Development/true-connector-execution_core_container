package it.eng.idsa.businesslogic.service.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("application.user")
public class UserConfiguration {

	private Api api;
	
	public Api getApiUser() {
		return api;
	}

	public void setApi(Api apiUser) {
		this.api = apiUser;
	}

	public static class Api {
		private String username;
		private String password;

		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	}
	
}
