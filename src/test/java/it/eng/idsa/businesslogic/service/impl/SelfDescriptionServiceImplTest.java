package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;

public class SelfDescriptionServiceImplTest {
	@Mock
	private SelfDescriptionConfiguration configuration;
	@Mock
	private SelfDescriptionManager selfDescriptionManager;

	private SelfDescriptionServiceImpl selfDefinitionService;

	private String infoModelVersion = "4.0.0";
	private URI connectorURI = URI.create("http://connectorURI");
	private URI curratorURI = URI.create("http://curratorURI");
	private URI maintainerURI = URI.create("http://maintainerURI");
	private String title = "Self desctiption title";
	private String description = "Self desctiption desctiption";

	private URI endpointUri = URI.create("https://defaultEndpoint");
	@Mock
	private Connector connectorMock;

	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException {
		MockitoAnnotations.initMocks(this);
		when(configuration.getInformationModelVersion()).thenReturn(infoModelVersion);
		when(configuration.getConnectorURI()).thenReturn(connectorURI);
		when(configuration.getTitle()).thenReturn(title);
		when(configuration.getDescription()).thenReturn(description);
		when(configuration.getCurator()).thenReturn(curratorURI);
		when(configuration.getDefaultEndpoint()).thenReturn(endpointUri);
		when(configuration.getMaintainer()).thenReturn(maintainerURI);
		selfDefinitionService = new SelfDescriptionServiceImpl(configuration, selfDescriptionManager);
		selfDefinitionService.initConnector();
	}

	@Test
	@Disabled("Need to review this test, to provide valid connector so we can check if result is valid")
	public void getConnectionString() throws IOException {
		when(selfDescriptionManager.getValidConnector(any(Connector.class))).thenReturn(connectorMock);
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
}
