package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.util.UtilMessageService;

public class RejectionMessageServiceImplTest {

	@InjectMocks
	RejectionMessageServiceImpl rejectionMessageServiceImpl;

	@Mock
	private DapsTokenProviderService dapsProvider;
	@Mock
	private SelfDescriptionConfiguration selfDescriptionConfiguration;

	Message message;
	
	String tokenRejectionMessage = "NOT_AUTHENTICATED";
	String messageRejectionMessage  = "MALFORMED_MESSAGE";
	String communicationRejetionMessage = "NOT_FOUND";
	
	private final String IDS_PREFIX = "https://w3id.org/idsa/code/";
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(rejectionMessageServiceImpl, "informationModelVersion", "4.1.1", String.class);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(UtilMessageService.getDynamicAttributeToken());
		when(selfDescriptionConfiguration.getConnectorURI()).thenReturn(URI.create("http://auto-generated"));
		message = UtilMessageService.getArtifactResponseMessage();
	}

	@Test
	@Disabled("Currently not working - need to investigate if test makes sense")
	public void testSendRejectionMessageWithResultMessage() {
		
		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.RESULT_MESSAGE, message));
		String message = exception.getMessage();
		
		assertTrue(message.contains(IDS_PREFIX + tokenRejectionMessage));
	}

	@Test
	public void testSendRejectionMessageWithRejectionMessageCommonType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message));
		String message = exception.getMessage();
		
		assertTrue(message.contains(messageRejectionMessage));
	}
	
	
	@Test
	public void testSendRejectionMessageWithRejectionTokenType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, message));
		String message = exception.getMessage();
		
		assertTrue(message.contains(IDS_PREFIX + tokenRejectionMessage));
	}
	
	@Test
	public void testSendRejectionMessageWithRejectionMessageLocalIssuesType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, message));
		String message = exception.getMessage();
		
		assertTrue(message.contains(IDS_PREFIX + messageRejectionMessage));
	}
	
	@Test
	public void testSendRejectionMessageWithRejectionTokenLocalIssuesType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN_LOCAL_ISSUES, message));
		String message = exception.getMessage();
		
		assertTrue(message.contains(IDS_PREFIX + tokenRejectionMessage));

	}
	
	@Test
	public void testSendRejectionMessageWithRejectionCommunicationLocalIssuesType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, message));
		String message = exception.getMessage();
		
		assertTrue(message.contains(IDS_PREFIX + communicationRejetionMessage));
	}
}
