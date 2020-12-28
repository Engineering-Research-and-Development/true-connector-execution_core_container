package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.ArgumentMatchers.any;
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

import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.MultipartMessageKey;

public class SenderParseReceivedDataProcessorBodyBinaryTest {

	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	@Mock
	private Message messageOut;
	@Mock
	private de.fraunhofer.iais.eis.Message msg;
	@Mock
	private RejectionMessageService rejectionMessageService;

	private MultipartMessage multipartMessage;
	private String receivedDataBodyBinary;
	private Map<String, Object> httpHeaders;
	private Map<String, String> headerHeader;

	@InjectMocks
	private SenderParseReceivedDataProcessorBodyBinary processor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void processWithContentType() throws Exception {
		mockExchangeGetHttpHeaders(exchange);
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getArtifactRequestMessage())
				.withPayloadContent("foo bar")
				.build();
		receivedDataBodyBinary = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, false);
		msg = TestUtilMessageService.getArtifactRequestMessage();
		when(exchange.getMessage()).thenReturn(messageOut);
		when(messageOut.getBody(String.class)).thenReturn(receivedDataBodyBinary);

		processor.process(exchange);

		verify(messageOut).setBody(multipartMessage);

	}

	@Test
	public void processWithoutContentType() throws Exception {
		mockExchangeGetHttpHeaders(exchange);
		msg = TestUtilMessageService.getArtifactRequestMessage();
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(msg)
				.withPayloadContent("foo bar")
				.build();

		receivedDataBodyBinary = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
		when(exchange.getMessage()).thenReturn(messageOut);
		when(messageOut.getBody(String.class)).thenReturn(receivedDataBodyBinary);

		processor.process(exchange);

		verify(rejectionMessageService).sendRejectionMessage(any(RejectionMessageType.class),
				any(de.fraunhofer.iais.eis.Message.class));
	}

	@Test
	public void processWithInvalidContentTypeAndWithoutDaps() throws Exception {
		mockExchangeGetHttpHeaders(exchange);
		headerHeader = new HashMap<String, String>();
		headerHeader.put(MultipartMessageKey.CONTENT_DISPOSITION.label, "form-data; name=\"header\"");
		headerHeader.put(MultipartMessageKey.CONTENT_TYPE.label, ContentType.APPLICATION_XML.toString());
		headerHeader.put(MultipartMessageKey.CONTENT_LENGTH.label, "333");
		msg = TestUtilMessageService.getArtifactRequestMessage();
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(msg)
				.withHeaderHeader(headerHeader)
				.withPayloadContent("foo bar").build();
		receivedDataBodyBinary = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
		when(exchange.getMessage()).thenReturn(messageOut);
		when(messageOut.getBody(String.class)).thenReturn(receivedDataBodyBinary);

		processor.process(exchange);

		verify(rejectionMessageService).sendRejectionMessage(any(RejectionMessageType.class),
				any(de.fraunhofer.iais.eis.Message.class));
	}

	private void mockExchangeGetHttpHeaders(Exchange exchange) {
		when(exchange.getMessage()).thenReturn(message);
		httpHeaders = new HashMap<>();
		httpHeaders.put("Content-Type", "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6");
		httpHeaders.put("Forward-To", "https://forward.to.example");
		httpHeaders.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		when(message.getHeaders()).thenReturn(httpHeaders);
	}

}
