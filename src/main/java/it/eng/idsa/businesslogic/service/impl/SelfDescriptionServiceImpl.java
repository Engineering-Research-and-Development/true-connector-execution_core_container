
package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorUpdateMessageBuilder;
import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */

@Service
public class SelfDescriptionServiceImpl implements SelfDescriptionService {
	private static final Logger logger = LogManager.getLogger(SelfDescriptionServiceImpl.class);
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	Connector connector;
	
	private DapsService dapsService;
	private URI issuerConnectorURI;

	public SelfDescriptionServiceImpl(
			DapsService dapsService,
			SelfDescriptionConfiguration selfDescriptionConfiguration) {
		this.dapsService = dapsService;
		this.selfDescriptionConfiguration = selfDescriptionConfiguration;
	}

	@PostConstruct
	public void initConnector() throws ConstraintViolationException, URISyntaxException {
//		issuerConnectorURI = new URI("https://eng.true-connector.com/" + "igor");//RandomStringUtils.randomAlphabetic(3));
		issuerConnectorURI = selfDescriptionConfiguration.getConnectorURI();
		this.connector = new BaseConnectorBuilder(issuerConnectorURI)
				._maintainer_(new URI(selfDescriptionConfiguration.getSelfDescription().getCompanyURI()))
				._curator_(issuerConnectorURI)
				._resourceCatalog_((ArrayList<? extends ResourceCatalog>) this.getCatalog()) // Infomodel version 4.0.0
				._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE) // Infomodel version 4.0.0
				._maintainer_(new URI("https://maintainerURL" + RandomStringUtils.randomAlphabetic(3)))
				._inboundModelVersion_(Util.asList(new String[] { selfDescriptionConfiguration.getInformationMovelVersion() }))
				._title_(Util.asList(new TypedLiteral("Test Fraunhofer Digital Broker " + RandomStringUtils.randomAlphabetic(3))))
				._description_(Util.asList(new TypedLiteral("This is selfDescription description for Eng true connector")))
				._outboundModelVersion_(selfDescriptionConfiguration.getInformationMovelVersion())
//				._hasDefaultEndpoint_(new ConnectorEndpointBuilder(new URI("https://someURL/selfDescription")).build())
//				._hasEndpoint_(Util.asList(new ConnectorEndpointBuilder(new URI("https://someURL/incoming-data-channel/receivedMessage")).build()))
				.build();
	}

	public Connector getConnector() throws ConstraintViolationException, URISyntaxException {
		if (null == this.connector) {
			this.initConnector();
		}
		return this.connector;
	}

	@Override
	public String getConnectorAsString() {
		final Serializer serializer = new Serializer();
		String result = null;
		try {
			result = serializer.serialize(this.connector);
		} catch (IOException e) {
			logger.error(e);
		}
		return result;
	}

	private Resource getResource() throws ConstraintViolationException, URISyntaxException {
		Resource offeredResource = (new ResourceBuilder())
				._title_(Util.asList(
						new TypedLiteral(selfDescriptionConfiguration.getSelfDescription().getResource().getTitle())))
				._description_(Util.asList(
						new TypedLiteral(selfDescriptionConfiguration.getSelfDescription().getResource().getDescription())))
//				._contractOffer_(getContractOffers())
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"), 
						new TypedLiteral("broker"), new TypedLiteral("trueConnector")))
				._version_("1.0.0")
				._language_(Util.asList(Language.EN, Language.IT))
				.build();
		return offeredResource;
	}

	private ArrayList<ContractOffer> getContractOffers() throws ConstraintViolationException, URISyntaxException {
		Permission permission = new PermissionBuilder(
				new URI(selfDescriptionConfiguration.getSelfDescription().getContractOffer().getPermission())).build();
		ContractOffer contractOffer = new ContractOfferBuilder(new URI("http://example.com/ids-profile/1234"))
				._provider_(new URI(selfDescriptionConfiguration.getSelfDescription().getContractOffer().getProvider()))
				._permission_(Util.asList(permission))
				.build();
		
			ArrayList<ContractOffer> contractOfferList = new ArrayList<>();
			contractOfferList.add(contractOffer);
			return contractOfferList;
	}

	private java.util.List<ResourceCatalog> getCatalog() throws ConstraintViolationException, URISyntaxException {
		ResourceCatalog catalog = (new ResourceCatalogBuilder()
				._offeredResource_(Util.asList(new Resource[] { this.getResource() })).build());
		java.util.List<ResourceCatalog> catalogList = new ArrayList<>();
		catalogList.add(catalog);
		return catalogList;
	}

	@Override
	public Message getConnectorAvailbilityMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		DynamicAttributeToken securityToken = getJwToken();

		return new ConnectorUpdateMessageBuilder(new URI("https://w3id.org/idsa/autogen/connectorAvailableMessage/" + UUID.randomUUID().toString()))
		._securityToken_(securityToken)
		._senderAgent_(new URI("http://example.org" + RandomStringUtils.randomAlphabetic(3)))
		._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
		._issuerConnector_(issuerConnectorURI)
		._modelVersion_(selfDescriptionConfiguration.getInformationMovelVersion())
		._affectedConnector_(connector.getId())
		.build();
	}

	private DynamicAttributeToken getJwToken() throws URISyntaxException {
		String jwToken = dapsService.getJwtToken();
		DynamicAttributeToken securityToken = 
				new DynamicAttributeTokenBuilder(new URI("https://w3id.org/idsa/autogen/dynamicAttributeToken/ec96a61a-8725-4227-90e7-a1976e6d4dfe"))
				._tokenValue_(jwToken)
				._tokenFormat_(TokenFormat.JWT)
				.build();
		return securityToken;
	}

	@Override
	public Message getConnectorUpdateMessage()
			throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		
		DynamicAttributeToken securityToken = getJwToken();
	
		return new ConnectorUpdateMessageBuilder(new URI("https://w3id.org/idsa/autogen/connectorUpdateMessage/6d875403-cfea-4aad-979c-3515c2e71967"))
				._securityToken_(securityToken)
				._senderAgent_(new URI("http://example.org"))
				._modelVersion_(selfDescriptionConfiguration.getInformationMovelVersion())
				._issuerConnector_(issuerConnectorURI)
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorUnavailableMessage()
			throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {

		DynamicAttributeToken securityToken = getJwToken();
		// Mandatory fields are: affectedConnector, securityToken, issuerConnector, senderAgent, modelVersion, issued
		
		return new ConnectorUnavailableMessageBuilder(new URI("http://industrialdataspace.org/connectorUnavailableMessage/"+ UUID.randomUUID().toString()))
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._affectedConnector_(connector.getId())
				._modelVersion_(selfDescriptionConfiguration.getInformationMovelVersion())
				._issuerConnector_(issuerConnectorURI)
				._securityToken_(securityToken)
				._senderAgent_(new URI("http://example.org"))
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				.build();
	}

	@Override
	public Message getConnectorInactiveMessage()
			throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		
		DynamicAttributeToken securityToken = getJwToken();
		
		return new ConnectorUnavailableMessageBuilder(new URI("https://w3id.org/idsa/autogen/connectorInactiveMessage/8ea20fa1-7258-41c9-abc2-82c787d50ec3"))
				._modelVersion_(selfDescriptionConfiguration.getInformationMovelVersion())
				._issuerConnector_(issuerConnectorURI)
				._senderAgent_(new URI("http://example.org"))
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._securityToken_(securityToken)
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorQueryMessage()
			throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		DynamicAttributeToken securityToken = getJwToken();
		
		return new QueryMessageBuilder(new URI("https://w3id.org/idsa/autogen/queryMessage" + UUID.randomUUID().toString()))
				._securityToken_(securityToken)
				._senderAgent_(new URI("http://example.org"))
				._modelVersion_(selfDescriptionConfiguration.getInformationMovelVersion())
				._issuerConnector_(issuerConnectorURI)
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._queryLanguage_(QueryLanguage.SPARQL)
				._queryScope_(QueryScope.ALL)
				.build();
	}

}
