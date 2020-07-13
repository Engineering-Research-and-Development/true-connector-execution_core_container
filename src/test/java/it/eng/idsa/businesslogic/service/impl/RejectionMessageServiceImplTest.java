package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

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
	
	String directory = "./src/test/resources/RejectionMessageServiceImplTest/";

	@Before
	public void init() {
		String fraunhoferMessageAsString = null;
		try {
			fraunhoferMessageAsString = new String(Files.readAllBytes(Paths.get(directory + "fraunhoferMessageAsString.txt")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		message = multipartMessageServiceImpl.getMessage(fraunhoferMessageAsString);
	}

	@Test
	public void testSendRejectionMessageWithResultMessage() {
		
		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.RESULT_MESSAGE, message));
		
		
		String message = exception.getMessage();
		
		assertFalse(message.contains(rejectionReasonTemplate));
	}

	@Test
	public void testSendRejectionMessageWithRejectionMessageCommonType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message));
		
		
		String message = exception.getMessage();
		
		assertTrue(message.contains(rejectionReasonTemplate + messageRejectionMessage));
	}
	
	
	@Test
	public void testSendRejectionMessageWithRejectionTokenType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, message));
		
		
		String message = exception.getMessage();
		
		assertTrue(message.contains(rejectionReasonTemplate + tokenRejectionMessage));
	}
	
	@Test
	public void testSendRejectionMessageWithRejectionMessageLocalIssuesType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, message));
		
		
		String message = exception.getMessage();
		
		assertTrue(message.contains(rejectionReasonTemplate + messageRejectionMessage));
	}
	
	@Test
	public void testSendRejectionMessageWithRejectionTokenLocalIssuesType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN_LOCAL_ISSUES, message));
		
		
		String message = exception.getMessage();
		
		assertTrue(message.contains(rejectionReasonTemplate + tokenRejectionMessage));
	}
	
	@Test
	public void testSendRejectionMessageWithRejectionCommunicationLocalIssuesType() {

		ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
				() -> rejectionMessageServiceImpl.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, message));
		
		
		String message = exception.getMessage();
		
		assertTrue(message.contains(rejectionReasonTemplate + communicationRejetionMessage));
	}

}
