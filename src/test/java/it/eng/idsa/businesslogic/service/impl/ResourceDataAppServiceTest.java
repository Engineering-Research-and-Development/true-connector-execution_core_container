package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.service.CommunicationService;

public class ResourceDataAppServiceTest {
	
	@InjectMocks
	private ResourceDataAppServiceImpl service;
	
	@Mock
	private CommunicationService communicationService;
	
	private static final String DATAAPP_ENDPOINT = "http://dataApp/";
	private static final String RESOURCE_TITLE = "Resource title";
	private static final String RESOURCE_DESCRIPTION = "Resource description";
	private static final String CONTRACT_OFFER_PROVIDER = "http://contract.offer.provider.url";
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		service = new ResourceDataAppServiceImpl(communicationService, DATAAPP_ENDPOINT);
	}
	
	@Test
	public void testGetResources() throws IOException {
		String resourceAsString = MultipartMessageServiceImpl.serializeMessage(getResource());
		when(communicationService.sendData(DATAAPP_ENDPOINT + "resource", "")).thenReturn(resourceAsString);
		Resource retVal = service.getResourceFromDataApp();
		assertNotNull(retVal);
		assertEquals(RESOURCE_TITLE, retVal.getTitle().get(0).getValue());
		assertEquals(RESOURCE_DESCRIPTION, retVal.getDescription().get(0).getValue());
	}
	
	@Test
	public void testGetContractOffer() throws IOException {
		String resourceAsString = MultipartMessageServiceImpl.serializeMessage(getContractOffer());
		when(communicationService.sendData(DATAAPP_ENDPOINT + "contractOffer", "")).thenReturn(resourceAsString);
		ContractOffer retVal = service.getContractOfferFromDataApp();
		assertNotNull(retVal);
		assertTrue(retVal.getPermission().get(0).getAction().contains(Action.USE));
		assertEquals(0, retVal.getProvider().compareTo(URI.create(CONTRACT_OFFER_PROVIDER)));
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
	
	private ContractOffer getContractOffer() {
		Permission permission = new PermissionBuilder()._action_(Util.asList(Action.USE)).build();
		return new ContractOfferBuilder()
				._provider_(URI.create(CONTRACT_OFFER_PROVIDER))
				._permission_(Util.asList(permission))
				.build();
	}
}
