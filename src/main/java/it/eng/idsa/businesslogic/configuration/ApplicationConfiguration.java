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
	private String dapsJWKSUrl;
	private String clearingHouseUrl;
	private String uriSchema;
	private String uriAuthority;
	private String uriConnector;
	private String activemqAddress;
	private String openDataAppReceiver;
	private String camelConsumerPort;
	private String camelProducerPort;

	private String basedir;

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

	public String getDapsJWKSUrl() {
		return dapsJWKSUrl;
	}

	public void setDapsJWKSUrl(String dapsJWKSUrl) {
		this.dapsJWKSUrl = dapsJWKSUrl;
	}

	public String getClearingHouseUrl() {
		return clearingHouseUrl;
	}

	public void setClearingHouseUrl(String clearingHouseUrl) {
		this.clearingHouseUrl = clearingHouseUrl;
	}

	public String getUriSchema() {
		return uriSchema;
	}

	public void setUriSchema(String uriSchema) {
		this.uriSchema = uriSchema;
	}

	public String getUriAuthority() {
		return uriAuthority;
	}

	public void setUriAuthority(String uriAuthority) {
		this.uriAuthority = uriAuthority;
	}

	public String getUriConnector() {
		return uriConnector;
	}

	public void setUriConnector(String uriConnector) {
		this.uriConnector = uriConnector;
	}

	public String getActivemqAddress() {
		return activemqAddress;
	}

	public void setActivemqAddress(String activemqAddress) {
		this.activemqAddress = activemqAddress;
	}

	public String getOpenDataAppReceiver() {
		return openDataAppReceiver;
	}

	public void setOpenDataAppReceiver(String openDataAppReceiver) {
		this.openDataAppReceiver = openDataAppReceiver;
	}

	public String getCamelProducerPort() {
		return camelProducerPort;
	}

	public void setCamelProducerPort(String camelProducerPort) {
		this.camelProducerPort = camelProducerPort;
	}

	public String getCamelConsumerPort() {
		return camelConsumerPort;
	}

	public void setCamelConsumerPort(String camelConsumerPort) {
		this.camelConsumerPort = camelConsumerPort;
	}

	public String getBasedir() {
		return basedir;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

}
