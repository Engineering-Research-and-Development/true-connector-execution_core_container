package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.util.MultipartMessageUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class OriginalMessageProcessorTest {

	@InjectMocks
	private OriginalMessageProcessor processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;
	@Mock
	private Map<String, Object> exchangeProperties;
	
	private MultipartMessage multipartMessage;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(exchange.getProperties()).thenReturn(exchangeProperties);
		multipartMessage = MultipartMessageUtil.getMultipartMessage(UtilMessageService.getArtifactRequestMessage());
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
	}
	
	@Test
	public void processOriginalMessage() throws Exception {
		processor.process(exchange);
		verify(exchangeProperties).put("Original-Message-Header", multipartMessage.getHeaderContent());
		verify(exchangeProperties).put("Original-Message-Payload", multipartMessage.getPayloadContent());
	}
}
