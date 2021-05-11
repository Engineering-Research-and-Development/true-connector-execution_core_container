package it.eng.idsa.businesslogic.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.multipart.util.TestUtilMessageService;

public class MockUtil {
	
	private static final String INFO_MODEL_VERSION = "4.0.6";
	
	/**
	 * Creates rejectionService, mocks dapsProvider call and sets InfoModel version
	 * @param rejectionMessageService
	 * @param dapsProvider
	 * @return
	 */
	public static RejectionMessageService mockRejectionService(RejectionMessageService rejectionMessageService) {
		rejectionMessageService = new RejectionMessageServiceImpl();
		SelfDescriptionConfiguration selfDescriptionConfiguration = mock(SelfDescriptionConfiguration.class);
		DapsTokenProviderService dapsProvider = mock(DapsTokenProviderService.class);
		ReflectionTestUtils.setField(rejectionMessageService, "dapsProvider", dapsProvider);
		ReflectionTestUtils.setField(rejectionMessageService, "informationModelVersion", INFO_MODEL_VERSION);
		ReflectionTestUtils.setField(rejectionMessageService, "selfDescriptionConfiguration", selfDescriptionConfiguration, SelfDescriptionConfiguration.class);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(TestUtilMessageService.getDynamicAttributeToken());
		when(selfDescriptionConfiguration.getConnectorURI()).thenReturn(TestUtilMessageService.ISSUER_CONNECTOR);
		return rejectionMessageService;
	}

}
