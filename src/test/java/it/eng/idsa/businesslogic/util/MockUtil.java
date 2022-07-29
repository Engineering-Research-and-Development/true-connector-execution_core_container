package it.eng.idsa.businesslogic.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MockUtil {
	
	
	/**
	 * Creates rejectionService, mocks dapsProvider call and sets ConnectorURI
	 * @param rejectionMessageService
	 * @param dapsProvider
	 * @return
	 */
	public static RejectionMessageService mockRejectionService(RejectionMessageService rejectionMessageService) {
		SelfDescriptionConfiguration selfDescriptionConfiguration = mock(SelfDescriptionConfiguration.class);
		DapsTokenProviderService dapsProvider = mock(DapsTokenProviderService.class);
		rejectionMessageService = new RejectionMessageServiceImpl(selfDescriptionConfiguration);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(UtilMessageService.getDynamicAttributeToken());
		when(selfDescriptionConfiguration.getConnectorURI()).thenReturn(UtilMessageService.ISSUER_CONNECTOR);
		return rejectionMessageService;
	}

}
