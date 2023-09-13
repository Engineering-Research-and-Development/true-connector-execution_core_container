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

public class MapMultipartToIDSCP2Test {

	@InjectMocks
	private MapMultipartToIDSCP2 processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;
	@Mock
	private Map<String, Object> exchangeProperties;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(processor, "isEnabledIdscp2", Boolean.TRUE, Boolean.class);
		ReflectionTestUtils.setField(processor, "receiver", Boolean.FALSE, Boolean.class);
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(exchange.getProperties()).thenReturn(exchangeProperties);
	}
	
	@Test
	public void multiaprtToIDSCPv2() throws Exception {
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(exchangeProperties);
		when(exchangeProperties.get("Forward-To")).thenReturn("http://forward.to.url:8080/test");
		
		processor.process(exchange);
		
		verify(camelMessage).setBody(multipartMessage.getPayloadContent());
		verify(camelMessage).setHeader("idscp2-header", multipartMessage.getHeaderContentString());
		verify(exchange).setProperty("host", "forward.to.url");
	}
}
