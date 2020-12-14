package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

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
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class SenderParseReceivedDataProcessorBodyFormDataTest {
	
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
	
	private de.fraunhofer.iais.eis.Message msg;
	private String headerAsString;
	
	@InjectMocks
	private SenderParseReceivedDataProcessorBodyFormData processor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		msg = TestUtilMessageService.getArtifactRequestMessage();
		headerAsString = TestUtilMessageService.getMessageAsString(msg);
	}

	@Test
	public void processWithoutDaps() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", false);
		mockExchangeGetHeaders(exchange);
		when(multipartMessageService.getMessage(headerAsString)).thenReturn(msg);
		when(exchange.getOut()).thenReturn(messageOut);
		
		processor.process(exchange);
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(msg)
				.withPayloadContent(PAYLOAD_STRING)
				.build();
		
		verify(messageOut).setBody(multipartMessage);
	}

	private void mockExchangeGetHeaders(Exchange exchange) {
		when(exchange.getIn()).thenReturn(message);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", ContentType.APPLICATION_JSON);
		headers.put("Forward-To", "https://forward.to.example");
		headers.put("header", headerAsString);
		headers.put("payload", PAYLOAD_STRING);
		when(message.getHeaders()).thenReturn(headers);
	}
}
