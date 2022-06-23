package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.ArtifactBuilder;
import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorUpdateMessageBuilder;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.KeyType;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.PublicKey;
import de.fraunhofer.iais.eis.PublicKeyBuilder;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.TextRepresentationBuilder;
import de.fraunhofer.iais.eis.TextResourceBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.RdfResource;
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
    private KeystoreProvider keystoreProvider;

	@Autowired
	public SelfDescriptionServiceImpl(
			SelfDescriptionConfiguration selfDescriptionConfiguration,
			DapsTokenProviderService dapsProvider,
			SelfDescriptionManager selfDescriptionManager,
			KeystoreProvider keystoreProvider) {
		this.selfDescriptionConfiguration = selfDescriptionConfiguration;
		this.dapsProvider = dapsProvider;
		this.selfDescriptionManager = selfDescriptionManager;
		this.keystoreProvider = keystoreProvider;
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
		issuerConnectorURI = SelfDescription.getInstance().getConnector().getId();
	}

	private Connector createDefaultSelfDescription() {
		logger.info("Creating default selfDescription from properties");
		issuerConnectorURI = selfDescriptionConfiguration.getConnectorURI();
		
		PublicKey publicKey = null;
        byte[] serverCertificate = null;
		try {
			serverCertificate = keystoreProvider.getCertificate().getEncoded();
			publicKey = new PublicKeyBuilder()._keyType_(KeyType.RSA)._keyValue_(serverCertificate).build();
		} catch (CertificateEncodingException | NullPointerException e) {
			logger.error("Error while creating PublicKey", e);
		}
        
		return new BaseConnectorBuilder(issuerConnectorURI)
				._maintainer_(selfDescriptionConfiguration.getMaintainer())
				._curator_(selfDescriptionConfiguration.getCurator())
				._resourceCatalog_(this.getCatalog())
				._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
				._publicKey_(publicKey)
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
		String stringConnector = UtilMessageService.getMessageAsString(SelfDescription.getInstance().getConnector());
		try {
			return selfDescriptionManager.getValidConnector(new Serializer().deserialize(stringConnector, Connector.class));
		} catch (IOException e) {
			logger.error("Error while deserializing connector", e);
			return null;
		}
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
		Artifact defaultArtifact = new ArtifactBuilder(URI.create("http://w3id.org/engrd/connector/artifact/1"))
			._creationDate_(DateUtil.now())
			.build();
		
		Resource offeredResource = (new TextResourceBuilder())
				._title_(Util.asList(new TypedLiteral("Default resource")))
				._description_(Util.asList(new TypedLiteral("Default resource description")))
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"),
						new TypedLiteral("TRUEConnector")))
				._version_("1.0.0")._language_(Util.asList(Language.EN, Language.IT))
				._modified_(DateUtil.now())
				._created_(DateUtil.now())
				._contractOffer_(Util.asList(createContractOffer()))
				._representation_(Util.asList(getTextRepresentation(defaultArtifact)))
				.build();
		
		List<ResourceCatalog> catalogList = new ArrayList<>();
		ArrayList<Resource> offeredResources = new ArrayList<>();
		offeredResources.add(offeredResource);
		catalogList.add(new ResourceCatalogBuilder()._offeredResource_(offeredResources).build());
		return catalogList;
	}
	
	private Representation getTextRepresentation(Artifact artifact) {
		return new TextRepresentationBuilder()
				._created_(DateUtil.now())
				._instance_(Util.asList(artifact))
				._language_(Language.EN)
				.build();
	}
	
	private ContractOffer createContractOffer() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC);
		
		Constraint before = new ConstraintBuilder()
				._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
				._operator_(BinaryOperator.AFTER)
				._rightOperand_(new RdfResource(dateTime.minusDays(7).format(formatter), 
						URI.create("http://www.w3.org/2001/XMLSchema#dateTimeStamp")))
				._pipEndpoint_(URI.create("http://pip.endpoint.after"))
				.build();
		
		Constraint after = new ConstraintBuilder()
				._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
				._operator_(BinaryOperator.BEFORE)
				._rightOperand_(new RdfResource(dateTime.plusMonths(1).format(formatter), 
						URI.create("http://www.w3.org/2001/XMLSchema#dateTimeStamp")))
				._pipEndpoint_(URI.create("http://pip.endpoint.before"))
				.build();
		
		Permission permission2 = new PermissionBuilder()
				._target_(URI.create("http://w3id.org/engrd/connector/artifact/1"))
				._action_(Util.asList(Action.USE))
				._constraint_(Util.asList(before, after))
				.build();
		
		return new ContractOfferBuilder()
				._provider_(issuerConnectorURI)
				._permission_(Util.asList(permission2))
				._contractDate_(DateUtil.now())
				._contractStart_(UtilMessageService.START_DATE)
				.build();
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
