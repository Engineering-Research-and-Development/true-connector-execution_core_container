package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sun.istack.ByteArrayDataSource;

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
	public void processBodyFormTest() throws Exception {
		mockExchangeGetHeaders(exchange);
		when(multipartMessageService.getMessage(headerAsString)).thenReturn(msg);
		
		processor.process(exchange);
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(msg)
				.withPayloadContent(PAYLOAD_STRING)
				.build();
		
		verify(message).setBody(multipartMessage);
	}

	private void mockExchangeGetHeaders(Exchange exchange) throws UnsupportedEncodingException {
		when(exchange.getMessage()).thenReturn(message);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", ContentType.APPLICATION_JSON);
		headers.put("Forward-To", "https://forward.to.example");
		ByteArrayDataSource headerBarrds = new ByteArrayDataSource(headerAsString.getBytes("UTF-8"), "application/json");
		headers.put("header", new DataHandler(headerBarrds));
		ByteArrayDataSource payloadBarrds = new ByteArrayDataSource(PAYLOAD_STRING.getBytes("UTF-8"), "application/json");
		headers.put("payload", new DataHandler(payloadBarrds));
		when(message.getHeaders()).thenReturn(headers);
	}
}
