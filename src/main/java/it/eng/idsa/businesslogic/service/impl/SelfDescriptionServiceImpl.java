package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.CRC32C;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.ArtifactBuilder;
import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorUpdateMessageBuilder;
import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.DataResourceBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.KeyType;
import de.fraunhofer.iais.eis.Language;
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
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.configuration.ShutdownConnector;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.service.resources.SelfDescription;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;
import it.eng.idsa.businesslogic.util.BigPayload;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */

@Service
public class SelfDescriptionServiceImpl implements SelfDescriptionService {
	
	private static final Logger logger = LoggerFactory.getLogger(SelfDescriptionServiceImpl.class);
	
	private Optional<DapsTokenProviderService> dapsProvider;
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	private Connector connector;
	private SelfDescriptionManager selfDescriptionManager;
	private URI issuerConnectorURI;
    private DapsKeystoreProvider keystoreProvider;
    private ShutdownConnector shutdown;

	public SelfDescriptionServiceImpl(
			SelfDescriptionConfiguration selfDescriptionConfiguration,	
			Optional<DapsTokenProviderService> dapsProvider,
			SelfDescriptionManager selfDescriptionManager,
			DapsKeystoreProvider keystoreProvider,
			ShutdownConnector shutdown) {
		this.selfDescriptionConfiguration = selfDescriptionConfiguration;
		this.dapsProvider = dapsProvider;
		this.selfDescriptionManager = selfDescriptionManager;
		this.keystoreProvider = keystoreProvider;
		this.shutdown = shutdown;
	}

	@PostConstruct
	public void initConnector() {
		// check if self description file exists
		logger.info("Preparing self description document...");
		connector = selfDescriptionManager.loadConnector();
		if(connector == null) {
			connector = createDefaultSelfDescription();
			selfDescriptionManager.saveConnector();
		}
		logger.info("Done creating self description document.");
		SelfDescription.getInstance().setBaseConnector(connector);
		issuerConnectorURI = SelfDescription.getInstance().getConnector().getId();
	}

	private Connector createDefaultSelfDescription() {
		logger.info("Creating default selfDescription from properties");
		try {
			issuerConnectorURI = selfDescriptionConfiguration.getConnectorURI();
		} catch (IllegalArgumentException e) {
			logger.error(e.getLocalizedMessage());
			shutdown.shutdownConnector();
		}
		
		PublicKey publicKey = null;
		try {
			var serverCertificate = keystoreProvider.getCertificate();
			if(serverCertificate != null) {
				publicKey = new PublicKeyBuilder(URI.create("https://w3id.org/idsa/autogen/publicKey/" + keystoreProvider.getCertificateSubject()))
						._keyType_(KeyType.RSA)
						._keyValue_(serverCertificate.getEncoded())
						.build();
			} else {
				logger.info("DAPS not configured so no PublicKey element present in Self description element");
			}
		} catch (CertificateEncodingException | NullPointerException e) {
			logger.error("Error while creating PublicKey", e);
		}
        
		Connector connector = new BaseConnectorBuilder(issuerConnectorURI)
				._maintainer_(selfDescriptionConfiguration.getMaintainer())
				._curator_(selfDescriptionConfiguration.getCurator())
				._resourceCatalog_(this.getCatalog())
				._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
				._publicKey_(publicKey)
				._inboundModelVersion_(Util.asList(selfDescriptionConfiguration.getSelfDescription().getInboundModelVersion().split(",")))
				._title_(Util.asList(new TypedLiteral(selfDescriptionConfiguration.getTitle())))
				._description_(Util.asList(new TypedLiteral(selfDescriptionConfiguration.getDescription())))
				._outboundModelVersion_(UtilMessageService.MODEL_VERSION)
				._hasDefaultEndpoint_(new ConnectorEndpointBuilder(selfDescriptionConfiguration.getDefaultEndpoint())
						._accessURL_(selfDescriptionConfiguration.getDefaultEndpoint())
						.build())
//				._hasEndpoint_(Util.asList(new ConnectorEndpointBuilder(new URI("https://someURL/incoming-data-channel/receivedMessage")).build()))
				.build();
		SelfDescription.getInstance().setBaseConnector(connector);
		return connector;
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
		URI defaultTarget = URI.create("http://w3id.org/engrd/connector/artifact/1");
		URI bigResource = URI.create("http://w3id.org/engrd/connector/artifact/big");
		URI csvResource = URI.create("http://w3id.org/engrd/connector/artifact/test1.csv");

		Artifact defaultArtifact = new ArtifactBuilder(defaultTarget)
			._creationDate_(DateUtil.normalizedDateTime())
			// this one is dynamic resource and checksum will change because of new Date when generating data
			._checkSum_("21683540")
			.build();
		
		Artifact bigArtifact = new ArtifactBuilder(bigResource)
				._creationDate_(DateUtil.normalizedDateTime())
				._checkSum_(String.valueOf(calculateChecksum(BigPayload.BIG_PAYLOAD.getBytes())))
				.build();
		
		Artifact csvArtifact = new ArtifactBuilder(csvResource)
				._creationDate_(DateUtil.now())
				.build();
		
		Resource offeredResource = (new TextResourceBuilder())
				._title_(Util.asList(new TypedLiteral("Default resource")))
				._description_(Util.asList(new TypedLiteral("Default resource description")))
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"),
						new TypedLiteral("TRUEConnector")))
				._version_("1.0.0")._language_(Util.asList(Language.EN, Language.IT))
				._modified_(DateUtil.normalizedDateTime())
				._created_(DateUtil.normalizedDateTime())
				._contractOffer_(Util.asList(createContractOffer(defaultTarget)))
				._representation_(Util.asList(getTextRepresentation(defaultArtifact)))
				.build();
		
		Resource offeredResourceBig = (new TextResourceBuilder())
				._title_(Util.asList(new TypedLiteral("World class literature")))
				._description_(Util.asList(new TypedLiteral("Used to verify large data transfer")))
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"),
						new TypedLiteral("TRUEConnector")))
				._version_("1.0.0")._language_(Util.asList(Language.EN, Language.IT))
				._modified_(DateUtil.normalizedDateTime())
				._created_(DateUtil.normalizedDateTime())
				._contractOffer_(Util.asList(createContractOffer(bigResource)))
				._representation_(Util.asList(getTextRepresentation(bigArtifact)))
				.build();
		
		Resource offeredResourceCsv = (new DataResourceBuilder())
				._title_(Util.asList(new TypedLiteral("CSV resource")))
				._description_(Util.asList(new TypedLiteral("Used to verify wss flow")))
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"),
						new TypedLiteral("TRUEConnector")))
				._version_("1.0.0")._language_(Util.asList(Language.EN, Language.IT))
				._modified_(DateUtil.now())
				._created_(DateUtil.now())
				._contractOffer_(Util.asList(createContractOffer(csvResource)))
				._representation_(Util.asList(getTextRepresentation(csvArtifact)))
				.build();
		
		List<ResourceCatalog> catalogList = new ArrayList<>();
		ArrayList<Resource> offeredResources = new ArrayList<>();
		offeredResources.add(offeredResource);
		offeredResources.add(offeredResourceBig);
		offeredResources.add(offeredResourceCsv);
		catalogList.add(new ResourceCatalogBuilder()._offeredResource_(offeredResources).build());
		return catalogList;
	}
	
	private Representation getTextRepresentation(Artifact artifact) {
		return new TextRepresentationBuilder()
				._created_(DateUtil.normalizedDateTime())
				._instance_(Util.asList(artifact))
				._language_(Language.EN)
				.build();
	}
	
	private ContractOffer createContractOffer(URI target) {
		//TODO uncomment this code when we upgrade infomodel; fix for compatibility with broker
		/*		
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
		*/
		Permission permission2 = new PermissionBuilder()
				._target_(target)
				._action_(Util.asList(Action.USE))
//				._constraint_(Util.asList(before, after))
				._title_(new TypedLiteral("Example Usage Policy"))
				._description_(new TypedLiteral("provide-access"))
				.build();
		
		return new ContractOfferBuilder()
				._provider_(issuerConnectorURI)
				._permission_(Util.asList(permission2))
				._contractDate_(DateUtil.normalizedDateTime())
				._contractStart_(UtilMessageService.START_DATE)
				.build();
	}

	@Override
	public Message getConnectorAvailbilityMessage() {
		return new ConnectorUpdateMessageBuilder()
			._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
			._issued_(DateUtil.normalizedDateTime())
			._issuerConnector_(issuerConnectorURI)
			._modelVersion_(UtilMessageService.MODEL_VERSION)
			._securityToken_(getDynamicAtributeToken())
			._affectedConnector_(connector.getId())
			.build();
	}

	@Override
	public Message getConnectorUpdateMessage() {
		return new ConnectorUpdateMessageBuilder()
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(issuerConnectorURI)
				._issued_(DateUtil.normalizedDateTime())
				._securityToken_(getDynamicAtributeToken())
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorUnavailableMessage() {

		// Mandatory fields are: affectedConnector, securityToken, issuerConnector, senderAgent, modelVersion, issued
		
		return new ConnectorUnavailableMessageBuilder()
				._issued_(DateUtil.normalizedDateTime())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(issuerConnectorURI)
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._securityToken_(getDynamicAtributeToken())
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorInactiveMessage() {
		return new ConnectorUnavailableMessageBuilder()
				._issued_(DateUtil.normalizedDateTime())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(issuerConnectorURI)
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._securityToken_(getDynamicAtributeToken())
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorQueryMessage() {
		return new QueryMessageBuilder()
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(issuerConnectorURI)
				._issued_(DateUtil.normalizedDateTime())
				._securityToken_(getDynamicAtributeToken())
				._queryLanguage_(QueryLanguage.SPARQL)
				._queryScope_(QueryScope.ALL)
				.build();
	}

	private DynamicAttributeToken getDynamicAtributeToken() {
		return dapsProvider.map(DapsTokenProviderService::getDynamicAtributeToken)
				.orElse(UtilMessageService.getDynamicAttributeToken());
	}

	private long calculateChecksum(final byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        final var checksum = new CRC32C();
        checksum.update(bytes, 0, bytes.length);
        return checksum.getValue();
    }
}
