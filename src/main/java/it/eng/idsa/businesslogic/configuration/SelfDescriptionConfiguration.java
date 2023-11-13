package it.eng.idsa.businesslogic.configuration;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.service.ProcessExecutor;

@Configuration
@ConfigurationProperties(prefix = "application")
public class SelfDescriptionConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(SelfDescriptionConfiguration.class);

	public static final String SELF_DESCRIPTION_FILE_NAME = "self_description.json";

	@Value("${server.ssl.enabled}")
	private boolean useHttps;
	@Value("${application.selfdescription.defaultEndpoint}")
	private String defaultEndpoint;
	@Autowired
	private ProcessExecutor processExecutor;

	@Value("${server.port}")
	private String serverPort;

	private int camelSenderPort;
	private int camelReceiverPort;
	private URI openDataAppReceiver;
	private String openDataAppReceiverRouter;
	private String eccHttpSendRouter;
	private boolean websocketIsEnabled;
	private boolean dataAppWebsocketIsEnabled;
	private String connectorid;

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

	public boolean isUseHttps() {
		return useHttps;
	}

	public void setConnectorid(String connectorid) {
		this.connectorid = connectorid;
	}

	public URI getConnectorURI() {
		if (StringUtils.isBlank(connectorid)) {
			throw new IllegalArgumentException("ConnectorId cannot be blank");
		}
		return URI.create(connectorid);
	}

	public URI getSenderAgent() {
		return URI.create("http://senderAgentURI.com");
	}

	public URI getDefaultEndpoint() {
		if (!StringUtils.isBlank(defaultEndpoint)) {
			return URI.create(defaultEndpoint);
		} else {
			String schema = useHttps ? "https" : "http";
			String port = System.getenv("PUBLIC_PORT");
			if (StringUtils.isEmpty(port)) {
				port = serverPort;
			}
			return URI.create(schema + "://" + getPublicIpAddress() + ":" + serverPort + "/");
		}
	}

	private String getPublicIpAddress() {
		String ipAddress = null;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			logger.info("Running on windows - no curl command, using InetAddress");
			try {
				ipAddress = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				logger.error("Error while attempting to get IP address", e);
			}
		} else {
			List<String> cmdList = new ArrayList<>();
			cmdList.add("/bin/sh");
			cmdList.add("-c");
			cmdList.add(" wget -qO - http://ipinfo.io/ip");

			ipAddress = processExecutor.executeProcess(cmdList);
		}
		return ipAddress;
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

	public String getFileLocation() {
		return this.selfDescription.getFilelocation();
	}

	public static class SelfDescription {
		private String description;
		private String title;
		private URI maintainer;
		private URI curator;
		private String filelocation;
		private String inboundModelVersion;

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

		public String getFilelocation() {
			return filelocation;
		}

		public void setFilelocation(String filelocation) {
			this.filelocation = filelocation;
		}

		public String getInboundModelVersion() {
			return inboundModelVersion;
		}

		public void setInboundModelVersion(String inboundModelVersion) {
			this.inboundModelVersion = inboundModelVersion;
		}
	}
}
