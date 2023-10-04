package it.eng.idsa.businesslogic.processor.receiver;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ReceiverParseReceivedConnectorRequestProcessorTest {
	
	@InjectMocks
	private ReceiverParseReceivedConnectorRequestProcessor processor;
	
	@Mock
	private HttpHeaderService headerService;
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	@Mock
	private Map<String, Object> headersAsMap;
	@Mock
	private de.fraunhofer.iais.eis.Message idsMessage;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(exchange.getMessage()).thenReturn(message);
	}
	
	@Test
	public void processHttpHeaderSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getHeaders()).thenReturn(headersAsMap);
		
		when(headerService.headersToMessage(headersAsMap)).thenReturn(idsMessage);
		
		processor.process(exchange);
		
		verify(message).setBody(any(MultipartMessage.class));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(any(), any(RejectionReason.class));
	}
	
	@Test
	public void processHttpHeaderException() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getHeaders()).thenReturn(headersAsMap);
		
		doThrow(NullPointerException.class).when(headerService).headersToMessage(headersAsMap);
		doThrow(ExceptionForProcessor.class)
			.when(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));

		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));
	}
	
	@Test
	public void processHttpHeaderException_MessageNull() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getHeaders()).thenReturn(headersAsMap);
		
		when(headerService.headersToMessage(headersAsMap)).thenReturn(null);
		doThrow(ExceptionForProcessor.class).when(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));
	}

	@Test
	public void processMixedSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_MIX, String.class);
		when(exchange.getMessage()).thenReturn(message);
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getArtifactRequestMessage())
				.withPayloadContent("foo bar")
				.build();
		when(message.getBody()).thenReturn(MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, false));
		
		processor.process(exchange);
		
		verify(message).setBody(any(MultipartMessage.class));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(any(), any(RejectionReason.class));
	}
	
	@Test
	public void processMixedException_InvalidMessage() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_MIX, String.class);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getBody()).thenReturn("MESSAGE BODY");
		doThrow(ExceptionForProcessor.class).when(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));

		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));
	}
	
	@Test
	public void processMixedException_MessageNull() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_MIX, String.class);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getBody()).thenReturn(null);
		
		doThrow(ExceptionForProcessor.class).when(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));
	}
	
	@Test
	public void processFormSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_BODY_FORM, String.class);
		when(exchange.getMessage()).thenReturn(message);
		Map<String, Object> parts = new HashMap<>();
		parts.put(MessagePart.HEADER, UtilMessageService.getMessageAsString(UtilMessageService.getArtifactRequestMessage()));
		parts.put(MessagePart.PAYLOAD, "PAYLOAD");
		when(message.getHeaders()).thenReturn(parts);
		
		processor.process(exchange);
		
		verify(message).setBody(any(MultipartMessage.class));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(any(), any(RejectionReason.class));
	}
	
	@Test
	public void processFormException_MessageNull() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_BODY_FORM, String.class);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getHeaders()).thenReturn(headersAsMap);
		
		when(headersAsMap.get(MessagePart.HEADER)).thenReturn(null);
		doThrow(ExceptionForProcessor.class).when(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));

		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });		
		
		verify(message, times(0)).setBody(any(MultipartMessage.class));
		verify(rejectionMessageService).sendRejectionMessage(any(), any(RejectionReason.class));
	}

}
