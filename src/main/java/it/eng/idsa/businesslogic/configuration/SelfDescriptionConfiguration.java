package it.eng.idsa.businesslogic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "selfdescription")
public class SelfDescriptionConfiguration {

	private String infoModelVersion;
	private String companyURI;
	private String connectorURI;
	private Resource resource = new Resource();
	private ContractOffer contractOffer = new ContractOffer();

	public String getInfoModelVersion() {
		return infoModelVersion;
	}

	public void setInfoModelVersion(String infoModelVersion) {
		this.infoModelVersion = infoModelVersion;
	}

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
