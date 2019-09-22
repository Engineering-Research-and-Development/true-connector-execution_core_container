package it.eng.idsa.businesslogic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
@ConfigurationProperties("application")
public class ApplicationConfiguration {
	
	private String targetDirectory;
	private String dapsUrl;
	private String keyStoreName;
	private String keyStorePassword;
	private String keystoreAliasName;
	private String connectorUUID;
	
	public String getTargetDirectory() {
		return targetDirectory;
	}
	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
	public String getDapsUrl() {
		return dapsUrl;
	}
	public void setDapsUrl(String dapsUrl) {
		this.dapsUrl = dapsUrl;
	}
	public String getKeyStoreName() {
		return keyStoreName;
	}
	public void setKeyStoreName(String keyStoreName) {
		this.keyStoreName = keyStoreName;
	}
	public String getKeyStorePassword() {
		return keyStorePassword;
	}
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}
	public String getKeystoreAliasName() {
		return keystoreAliasName;
	}
	public void setKeystoreAliasName(String keystoreAliasName) {
		this.keystoreAliasName = keystoreAliasName;
	}
	public String getConnectorUUID() {
		return connectorUUID;
	}
	public void setConnectorUUID(String connectorUUID) {
		this.connectorUUID = connectorUUID;
	}
	
	
}
