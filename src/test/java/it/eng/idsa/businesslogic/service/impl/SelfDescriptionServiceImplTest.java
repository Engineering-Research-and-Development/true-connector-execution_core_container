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
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsService;

public class SelfDescriptionServiceImplTest {
	
	@Mock
	private DapsService dapsService;
	@Mock
	private ResourceDataAppServiceImpl dataAppService;
	@Mock
	private SelfDescriptionConfiguration configuration;

	private SelfDescriptionServiceImpl selfDefinitionService;

	private String infoModelVersion = "4.0.0";
	private String connectorURI = "http://connectorURI";
	private String curratorURI = "http://curratorURI";
	private String maintainerURI = "http://maintainerURI";
	private String title = "Self desctiption title";
	private String description = "Self desctiption desctiption";

	private String RESOURCE_TITLE = "Resource title";
	private String RESOURCE_DESCRIPTION = "Resource description";

	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException {
		MockitoAnnotations.initMocks(this);
		when(dapsService.getJwtToken()).thenReturn("mockTokenValue");
		when(configuration.getInformationModelVersion()).thenReturn(infoModelVersion);
		when(configuration.getConnectorURI()).thenReturn(URI.create(connectorURI));
		when(configuration.getTitle()).thenReturn(title);
		when(configuration.getDescription()).thenReturn(description);
		when(configuration.getCurator()).thenReturn(URI.create(curratorURI));
		when(configuration.getMaintainer()).thenReturn(URI.create(maintainerURI));

		selfDefinitionService = new SelfDescriptionServiceImpl(dapsService, configuration, dataAppService);
		selfDefinitionService.initConnector();
	}

	@Test
	public void getConnectionString() throws IOException {
		String connectionString = selfDefinitionService.getConnectorAsString();
		assertNotNull(connectionString);
//		System.out.println(MultipartMessageProcessor.serializeToJsonLD(connectionString));

		assertTrue(connectionString.contains("ids:BaseConnector"));
		assertTrue(connectionString.contains("\"@type\" : \"ids:BaseConnector\""));
//		assertTrue(connectionString.contains("ids:resourceCatalog"));
		assertTrue(connectionString.contains("ids:inboundModelVersion"));
		assertTrue(connectionString.contains("ids:outboundModelVersion"));
		assertTrue(connectionString.contains("ids:description"));
		assertTrue(connectionString.contains("ids:maintainer"));
		assertTrue(connectionString.contains("ids:curator"));
		assertTrue(connectionString.contains("ids:title"));
		assertTrue(connectionString.contains("ids:securityProfile"));
//		assertTrue(connectionString.contains("ids:hasEndpoint"));
//		assertTrue(connectionString.contains("ids:hasDefaultEndpoint"));
	}
	
	@Test
	public void connectorAvailabilityMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message availabilityMessage = selfDefinitionService.getConnectorAvailbilityMessage();
		assertNotNull(availabilityMessage);
//		assertNotNull(availabilityMessage.getSecurityToken());
//		String ss = geObjectAsString(availabilityMessage);
//		System.out.println(ss);
	}
	
	@Test
	public void connectorInactiveMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message inactiveMessage = selfDefinitionService.getConnectorInactiveMessage();
//		assertNotNull(inactiveMessage.getSecurityToken());
		assertNotNull(inactiveMessage);
	}

	@Test
	public void connectorUpdateMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message updateMessage = selfDefinitionService.getConnectorUpdateMessage();
		assertNotNull(updateMessage.getSecurityToken());
		assertNotNull(updateMessage);
	}
	
	@Test
	public void connectorUnavailableMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message unavailableMessage = selfDefinitionService.getConnectorUnavailableMessage();
		assertNotNull(unavailableMessage.getSecurityToken());
		assertNotNull(unavailableMessage);
	}
	
	private void mockDataAppCalls() {
		when(dataAppService.getResourceFromDataApp()).thenReturn(getResource());
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
