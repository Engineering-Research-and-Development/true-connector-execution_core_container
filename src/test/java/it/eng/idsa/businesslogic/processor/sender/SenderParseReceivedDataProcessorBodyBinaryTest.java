package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.processor.util.TestUtilMessageService;
import it.eng.idsa.multipart.util.MultipartMessageKey;

public class SenderParseReceivedDataProcessorBodyBinaryTest {

	@Mock
	private Exchange exchange;
	@Mock
	private Message messageOut;
	@Mock
	private de.fraunhofer.iais.eis.Message msg;
	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
	private ProtocolValidationService protocolValidationService;

	private MultipartMessage multipartMessage;
	private String receivedDataBodyBinary;
	private Map<String, Object> httpHeaders;
	private Map<String, String> headerHeader;
	private String forwardTo;

	@InjectMocks
	private SenderParseReceivedDataProcessorBodyBinary processor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		forwardTo = "https://forward.to.example";
		when(protocolValidationService.validateProtocol(forwardTo, msg)).thenReturn(forwardTo);
	}

	@Test
	public void processWithContentType() throws Exception {
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getArtifactRequestMessage())
				.withPayloadContent("foo bar")
				.build();
		receivedDataBodyBinary = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, false);
		msg = TestUtilMessageService.getArtifactRequestMessage();
		when(exchange.getMessage()).thenReturn(messageOut);
		when(messageOut.getBody(String.class)).thenReturn(receivedDataBodyBinary);
		mockExchangeGetHttpHeaders();

		processor.process(exchange);

		verify(messageOut).setBody(multipartMessage);

	}
	
	@Test
	public void processWithoutContentType() throws Exception {
		msg = TestUtilMessageService.getArtifactRequestMessage();
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(msg)
				.withPayloadContent("foo bar")
				.build();

		receivedDataBodyBinary = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
		when(exchange.getMessage()).thenReturn(messageOut);
		when(messageOut.getBody(String.class)).thenReturn(receivedDataBodyBinary);
		mockExchangeGetHttpHeaders();

		processor.process(exchange);

		verify(messageOut).setBody(multipartMessage);
		verify(rejectionMessageService,times(0)).sendRejectionMessage(any(RejectionMessageType.class),
				any(de.fraunhofer.iais.eis.Message.class));
	}

	@Test
	public void processWithInvalidContentTypeAndWithoutDaps() throws Exception {
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
		mockExchangeGetHttpHeaders();

		processor.process(exchange);

		verify(messageOut).setBody(multipartMessage);
	}
	
	@Test
	public void processWithForwardTo_Null() throws Exception {
		forwardTo = null;
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getArtifactRequestMessage())
				.withPayloadContent("foo bar")
				.build();
		receivedDataBodyBinary = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, false);
		msg = TestUtilMessageService.getArtifactRequestMessage();
		when(exchange.getMessage()).thenReturn(messageOut);
		when(messageOut.getBody(String.class)).thenReturn(receivedDataBodyBinary);
		mockExchangeGetHttpHeaders();

		processor.process(exchange);

		verify(messageOut).setBody(multipartMessage);

	}

	private void mockExchangeGetHttpHeaders() {
		httpHeaders = new HashMap<>();
		httpHeaders.put("Content-Type", "multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6");
		httpHeaders.put("Forward-To", forwardTo);
		httpHeaders.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		when(messageOut.getHeaders()).thenReturn(httpHeaders);
	}

}
