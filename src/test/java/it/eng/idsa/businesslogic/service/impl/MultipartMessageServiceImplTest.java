package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MultipartMessageUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MultipartMessageServiceImplTest {
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	private MultipartMessageServiceImpl service;
	
	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException {
		MockitoAnnotations.openMocks(this);
		service = new MultipartMessageServiceImpl();
	}
	
	@Test
	public void addToken() {
		Message message = UtilMessageService.getArtifactRequestMessage();
		String token = "DUMMY_TOKEN_VALUE_UPDATE";
		String messageWithNewToken = service.addToken(message, token );
		assertNotNull(messageWithNewToken);
		assertTrue(messageWithNewToken.contains(token));
		assertFalse(messageWithNewToken.contains(UtilMessageService.TOKEN_VALUE));
	}
	
	@Test
	public void getToken() throws JsonProcessingException {
		String token = service.getToken(UtilMessageService.getArtifactRequestMessage());
		assertNotNull(token);
	}
	
	@Test
	public void addTokenToMultipart() {
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessageToken("TEST_TOKEN");
		assertEquals(UtilMessageService.TOKEN_VALUE, multipartMessage.getHeaderContent().getSecurityToken().getTokenValue());
		var result = service.addTokenToMultipartMessage(multipartMessage);
		assertEquals("TEST_TOKEN", result.getHeaderContent().getSecurityToken().getTokenValue());
	}
	
	@Test
	public void createMultipartMessage() {
		String header = UtilMessageService.getMessageAsString(UtilMessageService.getArtifactRequestMessage());
		var result = service.createMultipartMessage(header, "payload", "http://forward.to", ContentType.TEXT_PLAIN);
		
		assertNotNull(result);
	}
}
