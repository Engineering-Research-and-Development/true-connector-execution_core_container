
package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.IOUtils;
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
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.ResourceDataAppService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */

@Service
public class SelfDescriptionServiceImpl implements SelfDescriptionService {
	
	private static final Logger logger = LoggerFactory.getLogger(SelfDescriptionServiceImpl.class);
	
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	private Connector connector;
	private URI issuerConnectorURI;
	private ResourceDataAppService dataAppService;

	@Autowired
	public SelfDescriptionServiceImpl(
			SelfDescriptionConfiguration selfDescriptionConfiguration,
			ResourceDataAppService dataAppService) {
		this.selfDescriptionConfiguration = selfDescriptionConfiguration;
		this.dataAppService = dataAppService;
	}

	@PostConstruct
	public void initConnector() throws ConstraintViolationException {
		issuerConnectorURI = selfDescriptionConfiguration.getConnectorURI();
		this.connector = new BaseConnectorBuilder(issuerConnectorURI)
				._maintainer_(selfDescriptionConfiguration.getMaintainer())
				._curator_(selfDescriptionConfiguration.getCurator())
				._resourceCatalog_((ArrayList<? extends ResourceCatalog>) this.getCatalog())
				._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
				._inboundModelVersion_(Util.asList(new String[] { selfDescriptionConfiguration.getInformationModelVersion() }))
				._title_(Util.asList(new TypedLiteral(selfDescriptionConfiguration.getTitle())))
				._description_(Util.asList(new TypedLiteral(selfDescriptionConfiguration.getDescription())))
				._outboundModelVersion_(selfDescriptionConfiguration.getInformationModelVersion())
				._hasDefaultEndpoint_(new ConnectorEndpointBuilder(selfDescriptionConfiguration.getDefaultEndpoint())
						._accessURL_(selfDescriptionConfiguration.getDefaultEndpoint())
						.build())
//				._hasEndpoint_(Util.asList(new ConnectorEndpointBuilder(new URI("https://someURL/incoming-data-channel/receivedMessage")).build()))
				.build();
		
			try {
				logger.info(MultipartMessageProcessor.serializeToJsonLD(this.connector.getResourceCatalog()));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public Connector getConnector() {
		if (null == this.connector) {
			this.initConnector();
		}
		return this.connector;
	}

	@Override
	public String getConnectorSelfDescription() {
		String result = null;
		try {
			result = MultipartMessageProcessor.serializeToJsonLD(this.connector);
		} catch (IOException e) {
			logger.error("Error while serializing", e);
		}
		return result;
	}

//	private Resource getResource() throws ConstraintViolationException, URISyntaxException {
//		Resource offeredResource = (new ResourceBuilder())
//				._title_(Util.asList(
//						new TypedLiteral(selfDescriptionConfiguration.getSelfDescription().getResource().getTitle())))
//				._description_(Util.asList(
//						new TypedLiteral(selfDescriptionConfiguration.getSelfDescription().getResource().getDescription())))
////				._contractOffer_(getContractOffers())
//				._contentType_(ContentType.SCHEMA_DEFINITION)
//				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"), 
//						new TypedLiteral("broker"), new TypedLiteral("trueConnector")))
//				._version_("1.0.0")
//				._language_(Util.asList(Language.EN, Language.IT))
//				.build();
//		return offeredResource;
//	}

//	private ArrayList<ContractOffer> getContractOffers() throws ConstraintViolationException, URISyntaxException {
//		Permission permission = new PermissionBuilder(
//				new URI(selfDescriptionConfiguration.getSelfDescription().getContractOffer().getPermission())).build();
//		ContractOffer contractOffer = new ContractOfferBuilder(new URI("http://example.com/ids-profile/1234"))
//				._provider_(new URI(selfDescriptionConfiguration.getSelfDescription().getContractOffer().getProvider()))
//				._permission_(Util.asList(permission))
//				.build();
//		
//			ArrayList<ContractOffer> contractOfferList = new ArrayList<>();
//			contractOfferList.add(contractOffer);
//			return contractOfferList;
//	}

	private java.util.List<ResourceCatalog> getCatalog() {
		java.util.List<ResourceCatalog> catalogList = new ArrayList<>();
		try {
//			Resource resource = dataAppService.getResourceFromDataApp();
			ResourceCatalog catalog = null;
			try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("resource_catalog.json"))
		    {
		        String rc;
				try {
					rc = IOUtils.toString(inputStream, Charset.defaultCharset());
//					CollectionType typeReference =
//						    TypeFactory.defaultInstance().constructCollectionType(List.class, ResourceCatalog.class);
					catalog = new Serializer().deserialize(rc, ResourceCatalog.class);
//					catalogList = Arrays.asList(catalogListArray);
				} catch (IOException e) {
					e.printStackTrace();
				}
		    } catch (IOException e1) {
				e1.printStackTrace();
			}
			catalogList.add(catalog);
			
//			try {
//				logger.info(MultipartMessageProcessor.serializeToJsonLD(catalogList));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		} catch (ConstraintViolationException e) {
			e.printStackTrace();
		}
		return catalogList;
	}

	@Override
	public Message getConnectorAvailbilityMessage() throws ConstraintViolationException, DatatypeConfigurationException {

		return new ConnectorUpdateMessageBuilder()
			._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
			._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
			._issuerConnector_(issuerConnectorURI)
			._modelVersion_(selfDescriptionConfiguration.getInformationModelVersion())
			._affectedConnector_(connector.getId())
			.build();
	}


	@Override
	public Message getConnectorUpdateMessage()
			throws ConstraintViolationException, DatatypeConfigurationException {
		
	
		return new ConnectorUpdateMessageBuilder()
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._modelVersion_(selfDescriptionConfiguration.getInformationModelVersion())
				._issuerConnector_(issuerConnectorURI)
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorUnavailableMessage()
			throws ConstraintViolationException, DatatypeConfigurationException {

		// Mandatory fields are: affectedConnector, securityToken, issuerConnector, senderAgent, modelVersion, issued
		
		return new ConnectorUnavailableMessageBuilder()
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._affectedConnector_(connector.getId())
				._modelVersion_(selfDescriptionConfiguration.getInformationModelVersion())
				._issuerConnector_(issuerConnectorURI)
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				.build();
	}

	@Override
	public Message getConnectorInactiveMessage()
			throws ConstraintViolationException, DatatypeConfigurationException {
		
		return new ConnectorUnavailableMessageBuilder()
				._modelVersion_(selfDescriptionConfiguration.getInformationModelVersion())
				._issuerConnector_(issuerConnectorURI)
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._affectedConnector_(connector.getId())
				.build();
	}

	@Override
	public Message getConnectorQueryMessage()
			throws ConstraintViolationException, DatatypeConfigurationException {
		
		return new QueryMessageBuilder()
				._senderAgent_(selfDescriptionConfiguration.getSenderAgent())
				._modelVersion_(selfDescriptionConfiguration.getInformationModelVersion())
				._issuerConnector_(issuerConnectorURI)
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._queryLanguage_(QueryLanguage.SPARQL)
				._queryScope_(QueryScope.ALL)
				.build();
	}

}
