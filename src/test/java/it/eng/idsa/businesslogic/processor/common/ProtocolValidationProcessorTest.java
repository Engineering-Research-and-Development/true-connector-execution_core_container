package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ProtocolValidationProcessorTest {
	
	@InjectMocks
	private ProtocolValidationProcessor processor;
	
	@Mock
	private ProtocolValidationService protocolValidationService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message messageIn;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void protocolValidation_Enabled() throws Exception {
		ReflectionTestUtils.setField(processor, "enableProtocolValidation", true);
		mockExchange();
		processor.process(exchange);
		
		verify(protocolValidationService).validateProtocol(anyString(), any(de.fraunhofer.iais.eis.Message.class));
	}
	
	@Test
	public void protocolValidation_Disabled() throws Exception {
		ReflectionTestUtils.setField(processor, "enableProtocolValidation", false);
		
		verify(protocolValidationService, times(0)).validateProtocol(anyString(), any(de.fraunhofer.iais.eis.Message.class));
	}

	
	
	private void mockExchange() {
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getArtifactRequestMessage())
				.build();
		when(exchange.getMessage()).thenReturn(messageIn);
		when(messageIn.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(exchange.getProperty("Original-Message-Header")).thenReturn(UtilMessageService.getArtifactRequestMessage());
		when(messageIn.getHeader("Forward-To")).thenReturn("https://example.com");
	}
}
