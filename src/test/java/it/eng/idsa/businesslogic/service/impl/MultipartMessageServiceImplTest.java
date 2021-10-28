package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.exception.MultipartMessageProcessorException;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MultipartMessageServiceImplTest {
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	private MultipartMessageServiceImpl service;
	
	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException {
		MockitoAnnotations.initMocks(this);
		service = new MultipartMessageServiceImpl();
	}
	
	@Test
	public void testParseJsonLdMessageOnlyHeader() {
		MultipartMessage mm = new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getArtifactRequestMessage())
				.build();
		String multipartMessageAsString = MultipartMessageProcessor.multipartMessagetoString(mm);
		
		String header = service.getHeaderContentString(multipartMessageAsString);
		String payload = service.getPayloadContent(multipartMessageAsString);
		
		assertNotNull(header);
		assertNull(payload);
	}
	
	@Test
	public void getHeaderContentStringFail() {
		assertThrows(MultipartMessageProcessorException.class,
	            ()->{
	            	service.getHeaderContentString("INVALID MESSAGE");
	            });
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
}
