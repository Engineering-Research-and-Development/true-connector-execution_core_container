package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

public class RejectionMessageServiceImplTest {

	RejectionMessageServiceImpl rejectionMessageServiceImpl = new RejectionMessageServiceImpl();

	MultipartMessageServiceImpl multipartMessageServiceImpl = new MultipartMessageServiceImpl();

	Message message;
	
	String rejectionReasonTemplate = "https://w3id.org/idsa/code/";
	
	String tokenRejectionMessage = "NOT_AUTHENTICATED";
	
	String messageRejectionMessage  = "MALFORMED_MESSAGE";
	
	String communicationRejetionMessage = "NOT_FOUND";
	private final String IDS_PREFIX = "idsc:";
	
	String directory = "./src/test/resources/RejectionMessageServiceImplTest/";

	@BeforeEach
	public void init() {
		rejectionMessageServiceImpl.setInformationModelVersion("4.0.0");
		String fraunhoferMessageAsString = null;
		try {
			fraunhoferMessageAsString = new String(Files.readAllBytes(Paths.get(directory + "fraunhoferMessageAsString.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		message = multipartMessageServiceImpl.getMessage(fraunhoferMessageAsString);
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
