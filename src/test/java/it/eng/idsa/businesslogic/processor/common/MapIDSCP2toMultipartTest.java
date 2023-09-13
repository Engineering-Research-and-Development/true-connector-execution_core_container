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
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.util.MultipartMessageUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MapIDSCP2toMultipartTest {
	
	private String IDSCP2_HEADER = "idscp2-header";

	@InjectMocks
	private MapIDSCP2toMultipart processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;
	@Mock
	private Map<String, Object> exchangeProperties;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", Boolean.TRUE, Boolean.class);
		ReflectionTestUtils.setField(processor, "receiver", Boolean.TRUE, Boolean.class);
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(exchange.getProperties()).thenReturn(exchangeProperties);
	}
	
	@Test
	public void processIDSCP2HeaderAsString() throws Exception {
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		when(camelMessage.getHeader(IDSCP2_HEADER))
			.thenReturn(UtilMessageService.getMessageAsString(multipartMessage.getHeaderContent()));
		when(camelMessage.getBody(String.class)).thenReturn("PAYLOAD");
		processor.process(exchange);
		
		verify(camelMessage).setBody(multipartMessage);
		verify(exchangeProperties).put("Original-Message-Header", multipartMessage.getHeaderContent());
	}
	
	@Test
	public void processIDSCP2Header() throws Exception {
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		when(camelMessage.getHeader(IDSCP2_HEADER)).thenReturn(multipartMessage.getHeaderContent());
		when(camelMessage.getBody(String.class)).thenReturn("PAYLOAD");
		processor.process(exchange);
		
		verify(camelMessage).setBody(multipartMessage);
		verify(exchangeProperties).put("Original-Message-Header", multipartMessage.getHeaderContent());
	}
}
