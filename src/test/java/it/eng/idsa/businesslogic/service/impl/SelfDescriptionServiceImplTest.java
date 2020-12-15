package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration.SelfDescription;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration.SelfDescription.ContractOffer;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration.SelfDescription.Resource;
import it.eng.idsa.businesslogic.service.DapsService;

public class SelfDescriptionServiceImplTest {
	
	@Mock
	private DapsService dapsService;
	@Mock
	private SelfDescriptionConfiguration configuration;
	@Mock
	private SelfDescription sDProperties;
	@Mock
	private Resource resource;
	@Mock
	private ContractOffer contractOffer;

	private SelfDescriptionServiceImpl selfDefinitionService;

	private String infoModelVersion = "4.0.0";
	private String companyURI = "http://companyURI";
	private String connectorURI = "http://connectorURI";
	private String resourceTitle = "Resource title";
	private String resourceLang = "en";
	private String resourceDescription = "Resource description";

	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException {
		MockitoAnnotations.initMocks(this);
		when(dapsService.getJwtToken()).thenReturn("mockTokenValue");
		when(configuration.getSelfDescription()).thenReturn(sDProperties);
		when(sDProperties.getCompanyURI()).thenReturn(companyURI);
		when(sDProperties.getConnectorURI()).thenReturn(connectorURI);
		when(sDProperties.getResource()).thenReturn(resource);
		when(resource.getDescription()).thenReturn(resourceDescription);
		when(resource.getLanguage()).thenReturn(resourceLang);
		when(resource.getTitle()).thenReturn(resourceTitle);
		when(sDProperties.getContractOffer()).thenReturn(contractOffer);
		when(contractOffer.getPermission()).thenReturn("https://contractOfferPermission.com");
		when(contractOffer.getProfile()).thenReturn("https://contractOfferProfile.com");
		when(contractOffer.getProvider()).thenReturn("https://contractOfferProvider.com");
		when(contractOffer.getTarget()).thenReturn("https://contractOfferTarget.com");

		selfDefinitionService = new SelfDescriptionServiceImpl(dapsService, configuration);
		ReflectionTestUtils.setField(selfDefinitionService, "informationMovelVersion", infoModelVersion);
		selfDefinitionService.initConnector();

	}

	@Test
	public void getConnectionString() {
		String connectionString = selfDefinitionService.getConnectorAsString();
		assertNotNull(connectionString);
//		System.out.println(connectionString);

		assertTrue(connectionString.contains("ids:BaseConnector"));
		assertTrue(connectionString.contains("\"@type\" : \"ids:BaseConnector\""));
		assertTrue(connectionString.contains("ids:resourceCatalog"));
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
		assertNotNull(availabilityMessage.getSecurityToken());
//		String ss = geObjectAsString(availabilityMessage);
//		System.out.println(ss);
	}
	
	@Test
	public void connectorInactiveMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		Message inactiveMessage = selfDefinitionService.getConnectorInactiveMessage();
		assertNotNull(inactiveMessage.getSecurityToken());
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
	
	private String geObjectAsString(Object toSerialize) {
		final Serializer serializer = new Serializer();
		String result = null;
		try {
			result = serializer.serialize(toSerialize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
