package it.eng.idsa.businesslogic.configuration;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
public class SelfDescriptionConfiguration {

	@Value("${camel.component.http4.use-global-ssl-context-parameters}")
	private boolean useHttps;
	
	@Value("${information.model.version}")
	private String informationModelVersion;
	
	/**
	 * Used for http communication
	 */
	@Value("${http.port}")
	private String httpPort;
	
	/**
	 * Used for https communication
	 */
	@Value("${server.port}")
	private String serverPort;

	private int camelSenderPort;
	private int camelReceiverPort;
	private URI openDataAppReceiver;
	private String openDataAppReceiverRouter;
	private String eccHttpSendRouter;
	private boolean websocketIsEnabled;
	private boolean dataAppWebsocketIsEnabled;
	private String uriAuthority;
	private String uriConnector;
			
	private SelfDescription selfDescription = new SelfDescription();
	
	public int getCamelSenderPort() {
		return camelSenderPort;
	}

	public void setCamelSenderPort(int camelSenderPort) {
		this.camelSenderPort = camelSenderPort;
	}

	public int getCamelReceiverPort() {
		return camelReceiverPort;
	}

	public void setCamelReceiverPort(int camelReceiverPort) {
		this.camelReceiverPort = camelReceiverPort;
	}

	public URI getOpenDataAppReceiver() {
		return openDataAppReceiver;
	}

	public void setOpenDataAppReceiver(URI openDataAppReceiver) {
		this.openDataAppReceiver = openDataAppReceiver;
	}

	public String getOpenDataAppReceiverRouter() {
		return openDataAppReceiverRouter;
	}

	public void setOpenDataAppReceiverRouter(String openDataAppReceiverRouter) {
		this.openDataAppReceiverRouter = openDataAppReceiverRouter;
	}

	public String getEccHttpSendRouter() {
		return eccHttpSendRouter;
	}

	public void setEccHttpSendRouter(String eccHttpSendRouter) {
		this.eccHttpSendRouter = eccHttpSendRouter;
	}

	public boolean isWebsocketIsEnabled() {
		return websocketIsEnabled;
	}

	public void setWebsocketIsEnabled(boolean websocketIsEnabled) {
		this.websocketIsEnabled = websocketIsEnabled;
	}

	public boolean isDataAppWebsocketIsEnabled() {
		return dataAppWebsocketIsEnabled;
	}

	public void setDataAppWebsocketIsEnabled(boolean dataAppWebsocketIsEnabled) {
		this.dataAppWebsocketIsEnabled = dataAppWebsocketIsEnabled;
	}

	public SelfDescription getSelfDescription() {
		return selfDescription;
	}
	
	public void setSelfDescription(SelfDescription selfDescription) {
		this.selfDescription = selfDescription;
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
	
	public boolean isUseHttps() {
		return useHttps;
	}

	public String getInformationModelVersion() {
		return informationModelVersion;
	}

	public URI getConnectorURI() {
		String schema = useHttps ? "https:" : "http:";
		return URI.create(schema + uriAuthority + uriConnector);
	}
	
	public URI getSenderAgent() {
		return URI.create("http://senderAgentURI.com");
	}
	
	public URI getDefaultEndpoint() {
		String schema = useHttps ? "https" : "http";
		String port = useHttps ? serverPort : httpPort;
		String host;
		try {
			host = InetAddress.getLocalHost().getHostAddress();
			return URI.create(schema + "://" + host + ":" + port + "/selfDescription");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Shorthand methods to expose self description data
	 */
	public String getDescription() {
		return this.selfDescription.getDescription();
	}
	
	public String getTitle() {
		return this.selfDescription.getTitle();
	}
	
	public URI getMaintainer() {
		return this.selfDescription.getMaintainer();
	}
	
	public URI getCurator() {
		return this.selfDescription.getCurator();
	}
	
	public static class SelfDescription {
		private String description;
		private String title;
		private URI maintainer;
		private URI curator;
		
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public URI getMaintainer() {
			return maintainer;
		}

		public void setMaintainer(URI maintainer) {
			this.maintainer = maintainer;
		}

		public URI getCurator() {
			return curator;
		}

		public void setCurator(URI curator) {
			this.curator = curator;
		}
	}
}
