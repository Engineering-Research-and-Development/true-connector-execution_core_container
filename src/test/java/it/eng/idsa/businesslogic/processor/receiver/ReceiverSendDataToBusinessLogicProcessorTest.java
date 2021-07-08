package it.eng.idsa.businesslogic.processor.receiver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.util.TestUtilMessageService;

public class ReceiverSendDataToBusinessLogicProcessorTest {

	private static final String PAYLOAD_CONTENT = "Payload content";

	@InjectMocks
	private ReceiverSendDataToBusinessLogicProcessor processor;

	@Mock
	private HttpHeaderService headerService;
	@Mock
	private HeaderCleaner headerCleaner;
	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	@Mock
	private MultipartMessage multipartMessage;

	@Captor
	private ArgumentCaptor<String> key;
	@Captor
	private ArgumentCaptor<String> value;

	private Map<String, Object> headers;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getHeaders()).thenReturn(new HashMap<>());
	}

	@Test
	public void sendDataHttpheaders() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		when(message.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getPayloadContent()).thenReturn(PAYLOAD_CONTENT);
		when(multipartMessage.getHttpHeaders()).thenReturn(new HashMap<>());
		headers = createHeadersAsMessge();
		when(headerService.prepareMessageForSendingAsHttpHeaders(multipartMessage)).thenReturn(headers);

		processor.process(exchange);

		verify(message).setBody(PAYLOAD_CONTENT);
		verify(message).setHeaders(headers);
	}

	@Test
	public void sendDataMultipartForm() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_BODY_FORM, String.class);
		MultipartMessage mm = new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getRejectionMessage()).build();

		when(message.getBody(MultipartMessage.class)).thenReturn(mm);

		processor.process(exchange);
		
		verify(message).setHeader(key.capture(), value.capture());
    	assertEquals("Content-Type", key.getValue());
    	assertTrue(value.getValue().contains("multipart/form"));
	}
	
	@Test
	public void sendDataMultipartMixed() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_MIX, String.class);
		MultipartMessage mm = new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getRejectionMessage()).build();

		when(message.getBody(MultipartMessage.class)).thenReturn(mm);

		processor.process(exchange);
		
		verify(message).setHeader(key.capture(), value.capture());
    	assertEquals("Content-Type", key.getValue());
    	assertTrue(value.getValue().contains("multipart/mix"));
	}

	private Map<String, Object> createHeadersAsMessge() {
		headers = new HashMap<>();
		headers.put("Content-Type", ContentType.APPLICATION_JSON);
		headers.put("IDS-Messagetype", "ids:ArtifactRequestMessage");
		headers.put("IDS-Issued", "2019-05-27T13:09:42.306Z");
		headers.put("IDS-IssuerConnector", "http://true-connector/issuer");
		headers.put("IDS-Id",
				"https://w3id.org/idsa/autogen/artifactRequestMessage/d107ab28-5dc4-4f0c-a440-6d12ae6f2aab");
		headers.put("IDS-ModelVersion", "4.0.0");
		headers.put("IDS-RequestedArtifact", "http://true-connector/artifact/1");
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		return headers;
	}
}
