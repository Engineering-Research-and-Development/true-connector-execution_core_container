package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class SenderParseReceivedDataProcessorBodyFormDataTest {
	
	private final static String HEADER_STRING = "header";
	private final static String PAYLOAD_STRING = "payload";
	
	@Mock
	private MultipartMessageService multipartMessageService;
	@Mock
	private RejectionMessageService rejectionMessageService;

	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	@Mock
	private Message messageOut;
	@Mock
	private de.fraunhofer.iais.eis.Message msg;
	
	@InjectMocks
	private SenderParseReceivedDataProcessorBodyFormData processor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void processWithoutDaps() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", false);
		mockExchangeGetHeaders(exchange);
		when(multipartMessageService.getMessage(HEADER_STRING)).thenReturn(msg);
		when(exchange.getOut()).thenReturn(messageOut);
		
		processor.process(exchange);
		
		Map<String, String> multipartMap = message.getHeaders().entrySet().stream().filter(entry -> entry.getValue() instanceof String)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
		
		MultipartMessage multipartMessage = new MultipartMessage(multipartMap,
				null, msg, null, PAYLOAD_STRING, null, null,null);
		
		verify(messageOut).setBody(multipartMessage);
	}

	private void mockExchangeGetHeaders(Exchange exchange) {
		when(exchange.getIn()).thenReturn(message);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", ContentType.APPLICATION_JSON);
		headers.put("Forward-To", "https://forward.to.example");
		headers.put("header", HEADER_STRING);
		headers.put("payload", PAYLOAD_STRING);
		when(message.getHeaders()).thenReturn(headers);
	}
}
