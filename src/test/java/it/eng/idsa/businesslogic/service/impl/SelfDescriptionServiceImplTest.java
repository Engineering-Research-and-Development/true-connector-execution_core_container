package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;

public class SelfDescriptionServiceImplTest {
	@Mock
	private ResourceDataAppServiceImpl dataAppService;
	@Mock
	private SelfDescriptionConfiguration configuration;
	@Mock
	private DapsTokenProviderService dapsProvider;
	@Mock
	private DynamicAttributeToken dynamicAttributeToken;

	private SelfDescriptionServiceImpl selfDefinitionService;

	private String infoModelVersion = "4.0.0";
	private URI connectorURI = URI.create("http://connectorURI");
	private URI curratorURI = URI.create("http://curratorURI");
	private URI maintainerURI = URI.create("http://maintainerURI");
	private String title = "Self desctiption title";
	private String description = "Self desctiption desctiption";

	private String RESOURCE_TITLE = "Resource title";
	private String RESOURCE_DESCRIPTION = "Resource description";
	private URI endpointUri = URI.create("https://defaultEndpoint");
	private URI senderAgent = URI.create("https://senderAgent.com");

	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException {
		MockitoAnnotations.initMocks(this);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(dynamicAttributeToken);
		when(configuration.getInformationModelVersion()).thenReturn(infoModelVersion);
		when(configuration.getConnectorURI()).thenReturn(connectorURI);
		when(configuration.getTitle()).thenReturn(title);
		when(configuration.getDescription()).thenReturn(description);
		when(configuration.getCurator()).thenReturn(curratorURI);
		when(configuration.getDefaultEndpoint()).thenReturn(endpointUri);
		when(configuration.getMaintainer()).thenReturn(maintainerURI);
		when(configuration.getSenderAgent()).thenReturn(senderAgent);
		selfDefinitionService = new SelfDescriptionServiceImpl(configuration, dataAppService, dapsProvider);
		selfDefinitionService.initConnector();
	}

	@Test
	public void getConnectionString() throws IOException {
		String selfDescription = selfDefinitionService.getConnectorSelfDescription();
		assertNotNull(selfDescription);
		// System.out.println(selfDescription);

		assertTrue(selfDescription.contains("ids:BaseConnector"));
		assertTrue(selfDescription.contains("\"@type\" : \"ids:BaseConnector\""));
//		assertTrue(selfDescription.contains("ids:resourceCatalog"));
		assertTrue(selfDescription.contains("ids:inboundModelVersion"));
		assertTrue(selfDescription.contains("ids:outboundModelVersion"));
		assertTrue(selfDescription.contains("ids:description"));
		assertTrue(selfDescription.contains("ids:maintainer"));
		assertTrue(selfDescription.contains("ids:curator"));
		assertTrue(selfDescription.contains("ids:title"));
		assertTrue(selfDescription.contains("ids:securityProfile"));
//		assertTrue(selfDescription.contains("ids:hasEndpoint"));
		assertTrue(selfDescription.contains("ids:hasDefaultEndpoint"));
	}

	@Test
	public void connectorAvailabilityMessage()
			throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message availabilityMessage = selfDefinitionService.getConnectorAvailbilityMessage();
		assertNotNull(availabilityMessage);
//		String ss = geObjectAsString(availabilityMessage);
//		System.out.println(ss);
	}

	@Test
	public void connectorInactiveMessage()
			throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message inactiveMessage = selfDefinitionService.getConnectorInactiveMessage();
		assertNotNull(inactiveMessage);
	}

	@Test
	public void connectorUpdateMessage()
			throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message updateMessage = selfDefinitionService.getConnectorUpdateMessage();
		assertNotNull(updateMessage);
	}

	@Test
	public void connectorUnavailableMessage()
			throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message unavailableMessage = selfDefinitionService.getConnectorUnavailableMessage();
		assertNotNull(unavailableMessage);
	}

	private Resource getResource() {
		Resource offeredResource = (new ResourceBuilder())
				._title_(Util.asList(
						new TypedLiteral(RESOURCE_TITLE )))
				._description_(Util.asList(
						new TypedLiteral(RESOURCE_DESCRIPTION )))
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"), 
						new TypedLiteral("broker"), new TypedLiteral("trueConnector")))
				._version_("1.0.0")
				._language_(Util.asList(Language.EN, Language.IT))
				.build();
		return offeredResource;
	}
}
