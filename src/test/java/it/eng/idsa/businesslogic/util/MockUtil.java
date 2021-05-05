package it.eng.idsa.businesslogic.util;

import static org.mockito.Mockito.when;

import org.springframework.test.util.ReflectionTestUtils;

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
	public static RejectionMessageService mockRejectionService(RejectionMessageService rejectionMessageService, DapsTokenProviderService dapsProvider) {
		rejectionMessageService = new RejectionMessageServiceImpl();
		ReflectionTestUtils.setField(rejectionMessageService, "dapsProvider", dapsProvider);
		ReflectionTestUtils.setField(rejectionMessageService, "informationModelVersion", INFO_MODEL_VERSION);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(TestUtilMessageService.getDynamicAttributeToken());
		
		return rejectionMessageService;
	}

}
