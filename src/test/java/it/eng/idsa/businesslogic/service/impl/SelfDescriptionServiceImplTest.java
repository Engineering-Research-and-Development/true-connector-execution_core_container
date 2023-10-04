package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration.SelfDescription;
import it.eng.idsa.businesslogic.configuration.ShutdownConnector;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.impl.resources.SelfDescriptionUtil;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;

public class SelfDescriptionServiceImplTest {
	@Mock
	private SelfDescriptionConfiguration configuration;
	@Mock
	private SelfDescriptionManager selfDescriptionManager;
	@Mock
	private DapsTokenProviderService dapsProvider;
	@Mock
	private DynamicAttributeToken dynamicAttributeToken;
	@Mock
	private Connector connectorMock;
	@Mock
	private DapsKeystoreProvider keystoreProvider;
	@Mock
	private ShutdownConnector shutdown;
	@Mock 
	private X509Certificate cert;
	@Mock 
	private SelfDescription selfDescription;
	private SelfDescriptionServiceImpl selfDefinitionService;

	private URI connectorURI = URI.create("http://connectorURI");
	private URI curratorURI = URI.create("http://curratorURI");
	private URI maintainerURI = URI.create("http://maintainerURI");
	private String title = "Self desctiption title";
	private String description = "Self desctiption desctiption";

	private URI endpointUri = URI.create("https://defaultEndpoint");
	private URI senderAgent = URI.create("https://senderAgent.com");

	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException {
		MockitoAnnotations.openMocks(this);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(dynamicAttributeToken);
		when(configuration.getConnectorURI()).thenReturn(connectorURI);
		when(configuration.getTitle()).thenReturn(title);
		when(configuration.getDescription()).thenReturn(description);
		when(configuration.getCurator()).thenReturn(curratorURI);
		when(configuration.getDefaultEndpoint()).thenReturn(endpointUri);
		when(configuration.getMaintainer()).thenReturn(maintainerURI);
		when(configuration.getSenderAgent()).thenReturn(senderAgent);
		when(configuration.getSelfDescription()).thenReturn(selfDescription);
		when(selfDescription.getInboundModelVersion()).thenReturn("4.0.0,4.2.7");
		when(keystoreProvider.getCertificate()).thenReturn(cert);
		when(selfDescriptionManager.getValidConnector(any(Connector.class))).thenReturn(SelfDescriptionUtil.getBaseConnector());
		selfDefinitionService = new SelfDescriptionServiceImpl(configuration, Optional.ofNullable(dapsProvider), selfDescriptionManager, keystoreProvider, shutdown);
		selfDefinitionService.initConnector();
	}

	@Test
	public void getConnectionString() throws IOException {
		String selfDescription = selfDefinitionService.getConnectorSelfDescription();
		assertNotNull(selfDescription);
		// System.out.println(selfDescription);

		assertTrue(selfDescription.contains("ids:BaseConnector"));
		assertTrue(selfDescription.contains("\"@type\" : \"ids:BaseConnector\""));
		assertTrue(selfDescription.contains("ids:resourceCatalog"));
		assertTrue(selfDescription.contains("ids:inboundModelVersion"));
		assertTrue(selfDescription.contains("ids:outboundModelVersion"));
		assertTrue(selfDescription.contains("ids:description"));
		assertTrue(selfDescription.contains("ids:maintainer"));
		assertTrue(selfDescription.contains("ids:curator"));
		assertTrue(selfDescription.contains("ids:title"));
		assertTrue(selfDescription.contains("ids:securityProfile"));
		assertTrue(selfDescription.contains("ids:hasDefaultEndpoint"));
	}
	
	@Test
	public void invalidConnectorId() {
		when(selfDescriptionManager.loadConnector()).thenReturn(null);
		
		doThrow(IllegalArgumentException.class).when(configuration).getConnectorURI();

		selfDefinitionService.initConnector();
		
		verify(shutdown).shutdownConnector();
	}
	

	@Test
	public void connectorAvailabilityMessage() {
		Message availabilityMessage = selfDefinitionService.getConnectorAvailbilityMessage();
		assertNotNull(availabilityMessage);
//		String ss = geObjectAsString(availabilityMessage);
//		System.out.println(ss);
	}

	@Test
	public void connectorInactiveMessage() {
		Message inactiveMessage = selfDefinitionService.getConnectorInactiveMessage();
		assertNotNull(inactiveMessage);
	}

	@Test
	public void connectorUpdateMessage() {
		Message updateMessage = selfDefinitionService.getConnectorUpdateMessage();
		assertNotNull(updateMessage);
	}

	@Test
	public void connectorUnavailableMessage() {
		Message unavailableMessage = selfDefinitionService.getConnectorUnavailableMessage();
		assertNotNull(unavailableMessage);
	}
}