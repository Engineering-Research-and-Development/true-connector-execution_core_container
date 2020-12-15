package it.eng.idsa.businesslogic.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
public class SelfDescriptionConfiguration {

	@Value("${camel.component.http4.use-global-ssl-context-parameters}")
	private boolean useHttps;
	
	@Value("${information.model.version}")
	private String informationMovelVersion;

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

	public void setUseHttps(boolean useHttps) {
		this.useHttps = useHttps;
	}

	public String getInformationMovelVersion() {
		return informationMovelVersion;
	}

	public void setInformationMovelVersion(String informationMovelVersion) {
		this.informationMovelVersion = informationMovelVersion;
	}

	public URI getConnectorURI() {
		String schema = useHttps ? "https:" : "http:";
		return URI.create(schema + uriAuthority + uriConnector);
	}
	public static class SelfDescription {
		private String companyURI;
		private String connectorURI;
		
		private Resource resource = new Resource();
		private ContractOffer contractOffer = new ContractOffer();
		
		public String getCompanyURI() {
			return companyURI;
		}
		
		public void setCompanyURI(String companyURI) {
			this.companyURI = companyURI;
		}
		
		public String getConnectorURI() {
			return connectorURI;
		}
		
		public void setConnectorURI(String connectorURI) {
			this.connectorURI = connectorURI;
		}
		
		public Resource getResource() {
			return resource;
		}
		
		public void setResource(Resource resource) {
			this.resource = resource;
		}
		
		public ContractOffer getContractOffer() {
			return contractOffer;
		}
		
		public void setContractOffer(ContractOffer contractOffer) {
			this.contractOffer = contractOffer;
		}
		
		public class Resource {
			private String title;
			private String language;
			private String description;
			
			public String getTitle() {
				return title;
			}
			
			public void setTitle(String title) {
				this.title = title;
			}
			
			public String getLanguage() {
				return language;
			}
			
			public void setLanguage(String language) {
				this.language = language;
			}
			
			public String getDescription() {
				return description;
			}
			
			public void setDescription(String description) {
				this.description = description;
			}
		}
		
		public class ContractOffer {
			private String profile;
			private String target;
			private String provider;
			private String permission;
			
			public String getProfile() {
				return profile;
			}
			
			public void setProfile(String profile) {
				this.profile = profile;
			}
			
			public String getTarget() {
				return target;
			}
			
			public void setTarget(String target) {
				this.target = target;
			}
			
			public String getProvider() {
				return provider;
			}
			
			public void setProvider(String provider) {
				this.provider = provider;
			}
			
			public String getPermission() {
				return permission;
			}
			
			public void setPermission(String permission) {
				this.permission = permission;
			}
		}
	}
	
}
