package it.eng.idsa.businesslogic.processor.sender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.MockUtil;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.TestUtilMessageService;

public class SenderParseReceivedResponseMessageTest {

	private static final String PAYLOAD = "Payload response";

	@InjectMocks
	private SenderParseReceivedResponseMessage processor;
	
	@Captor 
	ArgumentCaptor<MultipartMessage> argCaptorMultipartMessage;
	
	@Mock
	private HttpHeaderService headerService;
	@Mock
	private MultipartMessageService multipartMessageService;
	@Mock
	private DapsTokenProviderService dapsProvider;
	
	@Mock
	private Exchange exchange;
	@Mock
	private org.apache.camel.Message camelMessage;
	@Mock
	private MultipartMessage multipartMessage;
	
	private Map<String, Object> headers = new HashMap<>();
	private Message message;
	
	private RejectionMessageService rejectionMessageService;

	private String headerAsString;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		message = TestUtilMessageService.getArtifactRequestMessage();
		headers.put("IDS-SecurityToken-TokenValue", TestUtilMessageService.TOKEN_VALUE);
		headerAsString = TestUtilMessageService.getMessageAsString(message);
	}
	
	@Test
	public void parseHttpHeaderResponse() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		mockExchangeHeaderAndBody();
		
		when(camelMessage.getBody(String.class)).thenReturn(PAYLOAD);
		when(headerService.getHeaderMessagePartFromHttpHeadersWithoutToken(headers)).thenReturn(headerAsString);
		
		processor.process(exchange);
		
		verify(camelMessage).setBody(argCaptorMultipartMessage.capture());
		assertEquals(TestUtilMessageService.TOKEN_VALUE, argCaptorMultipartMessage.getValue().getToken());
		assertEquals(PAYLOAD, argCaptorMultipartMessage.getValue().getPayloadContent());
		assertTrue(argCaptorMultipartMessage.getValue().getHeaderContent() instanceof ArtifactRequestMessage);
	}
	
	@Test
	public void parseNonHttpHeaderResponse_DapsEnabled() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", true);

		mockExchangeHeaderAndBody();
		headers.put(MessagePart.HEADER, headerAsString);
		headers.put(MessagePart.PAYLOAD, PAYLOAD);
		
		when(multipartMessageService.getMessage(headerAsString)).thenReturn(message);
		when(multipartMessageService.getToken(message)).thenReturn(TestUtilMessageService.TOKEN_VALUE);
		
		processor.process(exchange);
		
		verify(multipartMessageService).getToken(message);
		verify(camelMessage).setBody(argCaptorMultipartMessage.capture());
		assertEquals(TestUtilMessageService.TOKEN_VALUE, argCaptorMultipartMessage.getValue().getToken());
		assertEquals(PAYLOAD, argCaptorMultipartMessage.getValue().getPayloadContent());
		assertTrue(argCaptorMultipartMessage.getValue().getHeaderContent() instanceof ArtifactRequestMessage);
	}
	
	@Test
	public void parseResponseNoHeaderPresent() throws Exception {
		mockExchangeHeaderAndBody();
//		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER.label, String.class);
		
		rejectionMessageService = MockUtil.mockRejectionService(rejectionMessageService, dapsProvider);
		ReflectionTestUtils.setField(processor, "rejectionMessageService", 
				rejectionMessageService, RejectionMessageService.class);

		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
	}
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
	}
	
}