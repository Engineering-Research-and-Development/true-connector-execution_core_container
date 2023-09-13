package it.eng.idsa.businesslogic.processor.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;

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

public class ModifyPayloadProcessorTest {

	@InjectMocks
	private ModifyPayloadProcessor processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;

	@Captor 
	ArgumentCaptor<Message> argCaptorCamelMessage;
	
	private MultipartMessage multipartMessage;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(processor, "encodeDecodePayload", Boolean.TRUE, Boolean.class);
	}
	
	@Test
	public void modifyPayload() throws Exception {
		multipartMessage = MultipartMessageUtil.getMultipartMessage(UtilMessageService.getArtifactResponseMessage());
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		
		processor.process(exchange);
		
		verify(camelMessage).setBody(argCaptorCamelMessage.capture());
		
		MultipartMessage mm = (MultipartMessage) argCaptorCamelMessage.getValue();
		assertEquals(Base64.getEncoder().encodeToString(multipartMessage.getPayloadContent().getBytes()), mm.getPayloadContent());
	}
	
	@Test
	public void modifyPayload_not_ArtifactResponseMessage() throws Exception {
		when(exchange.getMessage()).thenReturn(camelMessage);
		multipartMessage = MultipartMessageUtil.getMultipartMessage(UtilMessageService.getArtifactRequestMessage());
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		processor.process(exchange);
		
		verify(camelMessage, times(0)).setBody(any(MultipartMessage.class));
	}
}
