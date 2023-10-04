package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.multipart.util.UtilMessageService;

public class PlatoonUsageControlServiceImplTest {

	@InjectMocks
	private PlatoonUsageControlServiceImpl platoonUsageControlServiceImpl;

	@Mock
	private CommunicationService communicationService;

	private String platoonURL = "http://platoon.com";

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(platoonUsageControlServiceImpl, "platoonURL", platoonURL);
		ReflectionTestUtils.setField(platoonUsageControlServiceImpl, "isEnabledUsageControl", true);
	}

	@Test
	public void testEnforceUsageControl_Succesfull() throws IOException {
		URI ucURI = URI.create("http://someContractAgreement.com/1");
		URI requestedArtifact = URI.create("http://w3id.org/engrd/connector/artifact/1");
		String ucObjet = "someUCObject";

		StringBuffer ucUrl = new StringBuffer().append(platoonURL).append("enforce/usage/agreement")
				.append("?contractAgreementUri=").append(ucURI).append("&requestedArtifact=").append(requestedArtifact)
				.append("&consuming=true");

		
		when(communicationService.sendDataAsJson(ucUrl.toString(), ucObjet, "application/json;charset=UTF-8"))
				.thenReturn("Usage allowed");

		String response = platoonUsageControlServiceImpl.enforceUsageControl(ucURI, requestedArtifact, ucObjet);

		assertEquals("Usage allowed", response);

	}

	@Test
	public void testEnforceUsageControl_Failed() throws IOException {
		URI ucURI = URI.create("someInvalidContractAgreement");
		URI requestedArtifact = URI.create("http://w3id.org/engrd/connector/artifact/1");
		String ucObjet = "someUCObject";

		StringBuffer ucUrl = new StringBuffer().append(platoonURL).append("enforce/usage/agreement")
				.append("?contractAgreementUri=").append(ucURI).append("&requestedArtifact=").append(requestedArtifact)
				.append("&consuming=true");

		when(communicationService.sendDataAsJson(ucUrl.toString(), ucObjet, "application/json;charset=UTF-8"))
				.thenReturn("Usage prohibited: Not valid contract agreement");

		String response = platoonUsageControlServiceImpl.enforceUsageControl(ucURI, requestedArtifact, ucObjet);

		assertEquals("Usage prohibited: Not valid contract agreement", response);

	}

	@Test
	public void testCreateUsageControlObject() {
		String payload = "somePayload";

		String ucObject = platoonUsageControlServiceImpl.createUsageControlObject(
				UtilMessageService.getArtifactRequestMessage(), UtilMessageService.getArtifactResponseMessage(),
				payload);

		assertEquals(payload, ucObject);
	}

	@Test
	public void testUploadPolicy_Successfull() {
		String payload = "validPolicy";
		String ucUrl = platoonURL + "contractAgreement/";

		when(communicationService.sendDataAsJson(ucUrl.toString(), payload, "application/json;charset=UTF-8"))
				.thenReturn("Policy uploaded");

		String response = platoonUsageControlServiceImpl.uploadPolicy(payload);

		assertEquals("Policy uploaded", response);
	}

	@Test
	public void testUploadPolicy_Failed() {
		String payload = "invalidPolicy";
		String ucUrl = platoonURL + "contractAgreement/";

		when(communicationService.sendDataAsJson(ucUrl.toString(), payload, "application/json;charset=UTF-8"))
				.thenReturn("Policy not uploaded");

		String response = platoonUsageControlServiceImpl.uploadPolicy(payload);

		assertEquals("Policy not uploaded", response);
	}

	@Test
	public void testUploadPolicyRollbackSuccess() {
		platoonUsageControlServiceImpl
				.rollbackPolicyUpload(UtilMessageService.getMessageAsString(UtilMessageService.getContractAgreement()));

		verify(communicationService).deleteRequest(any());
	}
}
