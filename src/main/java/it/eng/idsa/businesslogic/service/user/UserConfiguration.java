package it.eng.idsa.businesslogic.service.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("application.user")
public class UserConfiguration {

	private Api api;
	private Connector connector;
	
	public Api getApiUser() {
		return api;
	}

	public void setApi(Api apiUser) {
		this.api = apiUser;
	}

	public Connector getConnectorUser() {
		return connector;
	}

	public void setConnector(Connector connectorUser) {
		this.connector = connectorUser;
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
	
	public static class Connector {
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
