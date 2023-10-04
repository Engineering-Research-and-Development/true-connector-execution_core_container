package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;


import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.util.MultipartMessageUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class DeModifyPayloadProcessorTest {

	@InjectMocks
	private DeModifyPayloadProcessor processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;

	private MultipartMessage multipartMessage;
	
	@Captor 
	ArgumentCaptor<Message> argCaptorCamelMessage;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(processor, "encodeDecodePayload", Boolean.TRUE, Boolean.class);
		
		when(exchange.getMessage()).thenReturn(camelMessage);
	}
	
	@Test
	public void demodifyPayload() throws Exception {
		multipartMessage = MultipartMessageUtil.getMultipartMessage(UtilMessageService.getArtifactResponseMessage(), 
				Base64.getEncoder().encodeToString("PAYLOAD".getBytes()));
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		
		processor.process(exchange);
		
		verify(camelMessage).setBody(argCaptorCamelMessage.capture());
		
		MultipartMessage mm = (MultipartMessage) argCaptorCamelMessage.getValue();
		assertEquals("PAYLOAD", mm.getPayloadContent());
	}
	
	@Test
	public void demodifyPayload_not_ArtifactResponseMessage() throws Exception {
		multipartMessage = MultipartMessageUtil.getMultipartMessage(UtilMessageService.getArtifactRequestMessage());
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		processor.process(exchange);
		
		verify(camelMessage, times(0)).setBody(any(MultipartMessage.class));
	}

}
