package it.eng.idsa.businesslogic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("application.clearinghouse")
public class ClearingHouseConfiguration {

	private Boolean isEnabledClearingHouse;
	private String username;
	private String password;
	private String baseUrl;
	private String logEndpoint;
	private String processEndpoint;
	
	public Boolean getIsEnabledClearingHouse() {
		return isEnabledClearingHouse;
	}
	public void setIsEnabledClearingHouse(Boolean isEnabledClearingHouse) {
		this.isEnabledClearingHouse = isEnabledClearingHouse;
	}
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
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public String getLogEndpoint() {
		return logEndpoint;
	}
	public void setLogEndpoint(String logEndpoint) {
		this.logEndpoint = logEndpoint;
	}
	public String getProcessEndpoint() {
		return processEndpoint;
	}
	public void setProcessEndpoint(String processEndpoint) {
		this.processEndpoint = processEndpoint;
	}
}
