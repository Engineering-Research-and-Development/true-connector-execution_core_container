
package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorUpdateMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.service.resources.SelfDescription;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */

@Service
public class SelfDescriptionServiceImpl implements SelfDescriptionService {
	
	private static final Logger logger = LoggerFactory.getLogger(SelfDescriptionServiceImpl.class);
	
	private DapsTokenProviderService dapsProvider;
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	private Connector connector;
	private SelfDescriptionManager selfDescriptionManager;
	private URI issuerConnectorURI;

	@Autowired
	public SelfDescriptionServiceImpl(
			SelfDescriptionConfiguration selfDescriptionConfiguration,
			DapsTokenProviderService dapsProvider,
			SelfDescriptionManager selfDescriptionManager) {
		this.selfDescriptionConfiguration = selfDescriptionConfiguration;
		this.dapsProvider = dapsProvider;
		this.selfDescriptionManager = selfDescriptionManager;
	}

	@PostConstruct
	public void initConnector() {
		// check if self description file exists
		logger.info("Preparing self description document...");
		connector = selfDescriptionManager.loadConnector();
		if(connector == null) {
			connector = createDefaultSelfDescription();
		}
		logger.info("Done creating self description document.");
		SelfDescription.getInstance().setBaseConnector(connector);
		issuerConnectorURI = getConnector().getId();
	}

	private Connector createDefaultSelfDescription() {
		logger.info("Creating default selfDescription from properties");
		issuerConnectorURI = selfDescriptionConfiguration.getConnectorURI();
		return new BaseConnectorBuilder(issuerConnectorURI)
				._maintainer_(selfDescriptionConfiguration.getMaintainer())
				._curator_(selfDescriptionConfiguration.getCurator())
				._resourceCatalog_(this.getCatalog())
				._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
				._inboundModelVersion_(Util.asList(new String[] { UtilMessageService.MODEL_VERSION }))
				._title_(Util.asList(new TypedLiteral(selfDescriptionConfiguration.getTitle())))
				._description_(Util.asList(new TypedLiteral(selfDescriptionConfiguration.getDescription())))
				._outboundModelVersion_(UtilMessageService.MODEL_VERSION)
				._hasDefaultEndpoint_(new ConnectorEndpointBuilder(selfDescriptionConfiguration.getDefaultEndpoint())
						._accessURL_(selfDescriptionConfiguration.getDefaultEndpoint())
						.build())
//				._hasEndpoint_(Util.asList(new ConnectorEndpointBuilder(new URI("https://someURL/incoming-data-channel/receivedMessage")).build()))
				.build();
	}

	public Connector getConnector() {
		logger.debug("Parsing whole self description document to remove non valid resources");
		return selfDescriptionManager.getValidConnector(SelfDescription.getInstance().getConnector());
	}

	@Override
	public String getConnectorSelfDescription() {
		String result = null;
		try {
			result = MultipartMessageProcessor.serializeToJsonLD(getConnector());
		} catch (IOException e) {
			logger.error("Error while serializing", e);
		}
		return result;
	}


	private java.util.List<ResourceCatalog> getCatalog() {
		List<ResourceCatalog> catalogList = new ArrayList<>();
		ArrayList<Resource> offeredResources = new ArrayList<>();
		catalogList.add(new ResourceCatalogBuilder()._offeredResource_(offeredResources).build());
		return catalogList;
	}

	@Override
	public Message getConnectorAvailbilityMessage() {
		return new ConnectorUpdateMessageBuilder()
			._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
			._issued_(DateUtil.now())
			._issuerConnector_(issuerConnectorURI)
			._modelVersion_(UtilMessageService.MODEL_VERSION)
			._securityToken_(dapsProvider.getDynamicAtributeToken())
			._affectedConnector_(connector.getId())
			.build();
	}


	@Override
	public Message getConnectorUpdateMessage() {
		return new ConnectorUpdateMessageBuilder()
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(issuerConnectorURI)
				._issued_(DateUtil.now())
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorUnavailableMessage() {

		// Mandatory fields are: affectedConnector, securityToken, issuerConnector, senderAgent, modelVersion, issued
		
		return new ConnectorUnavailableMessageBuilder()
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(issuerConnectorURI)
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorInactiveMessage() {
		return new ConnectorUnavailableMessageBuilder()
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(issuerConnectorURI)
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorQueryMessage() {
		return new QueryMessageBuilder()
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(issuerConnectorURI)
				._issued_(DateUtil.now())
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._queryLanguage_(QueryLanguage.SPARQL)
				._queryScope_(QueryScope.ALL)
				.build();
	}

}
