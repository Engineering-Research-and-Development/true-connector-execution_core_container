package it.eng.idsa.businesslogic.processor.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ExceptionProcessorSenderTest {

	@InjectMocks
	private ExceptionProcessorSender processor;

	@Mock
	private HttpHeaderService headerService;
	@Mock
	private MultipartMessageService multipartMessageService;
	@Mock
	private Exchange exchange;
	@Mock
	private Message messageOut;
	@Mock
	private HeaderCleaner headerCleaner;
	@Mock
	private Exception exception;
	@Mock
	HttpEntity httpEntity;
	@Mock
	private org.apache.http.Header header;

	private MultipartMessage multipartMessage;
	private Map<String, Object> headers = new HashMap<>();
	private String exceptionMessage;
	
	 @Captor 
	 private ArgumentCaptor<String> key;
	 @Captor 
	 private ArgumentCaptor<String> value;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT)).thenReturn(exception);
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getRejectionMessage(RejectionReason.NOT_AUTHENTICATED))
				.build();
		exceptionMessage = MultipartMessageProcessor.multipartMessagetoString(multipartMessage);
		when(exception.getMessage()).thenReturn(exceptionMessage);
		
		when(exception.getMessage()).thenReturn(exceptionMessage);
	}

	@Test
	public void processExceptionHttpHeader() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.HTTP_HEADER, String.class);
		mockExchangeGetHttpHeaders(exchange);
		when(headerService.messageToHeaders(any(de.fraunhofer.iais.eis.Message.class))).thenReturn(headers);
		processor.process(exchange);

		verify(messageOut).setBody(multipartMessage);
		verify(messageOut).setHeaders(headers);

	}

	@Test
	public void sendDataToMultipartForm() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.MULTIPART_BODY_FORM,
				String.class);
		InputStream stubInputStream = IOUtils.toInputStream("some test data for my input stream", "UTF-8");
		when(exchange.getMessage()).thenReturn(messageOut);
		when(multipartMessageService.createMultipartMessage(any(String.class), isNull(), isNull(),
				any(ContentType.class))).thenReturn(httpEntity);
		when(httpEntity.getContentType()).thenReturn(header);
		when(httpEntity.getContent()).thenReturn(stubInputStream);
		when(header.getValue())
				.thenReturn("multipart/form; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6");

		processor.process(exchange);

    	verify(messageOut).setBody(IOUtils.toInputStream("some test data for my input stream", "UTF-8").readAllBytes());
    	verify(messageOut).setHeader("Content-Type", "multipart/form; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6");

	}

	@Test
	public void sendDataToMultipartMixed() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.MULTIPART_MIX,
				String.class);
		when(exchange.getMessage()).thenReturn(messageOut);

		processor.process(exchange);

		verify(messageOut).setHeader(key.capture(), value.capture());
    	assertEquals("Content-Type", key.getValue());
    	assertTrue(value.getValue().contains("multipart/mixed"));
	}
	
	private void mockExchangeGetHttpHeaders(Exchange exchange) {
		when(exchange.getMessage()).thenReturn(messageOut);
		headers = new HashMap<>();
		headers.put("Content-Type", ContentType.APPLICATION_JSON);
		headers.put("IDS-Messagetype", "ids:ArtifactRequestMessage");
		headers.put("IDS-Issued", "2019-05-27T13:09:42.306Z");
		headers.put("IDS-IssuerConnector", "http://true-connector/issuer");
		headers.put("IDS-Id",
				"https://w3id.org/idsa/autogen/artifactRequestMessage/d107ab28-5dc4-4f0c-a440-6d12ae6f2aab");
		headers.put("IDS-ModelVersion", "4.2.7");
		headers.put("IDS-RequestedArtifact", "http://true-connector/artifact/1");
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		when(messageOut.getHeaders()).thenReturn(headers);
	}

}
