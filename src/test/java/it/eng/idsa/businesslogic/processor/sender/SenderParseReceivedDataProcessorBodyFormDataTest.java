package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.attachment.Attachment;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sun.istack.ByteArrayDataSource;

import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SenderParseReceivedDataProcessorBodyFormDataTest {
	
	private final static String PAYLOAD_STRING = "payload";
	
	@Mock
	private RejectionMessageService rejectionMessageService;

	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	@Mock
	private AttachmentMessage attachmentMessage;
	@Mock
	private Attachment attachment;
	
	private de.fraunhofer.iais.eis.Message msg;
	private String headerAsString;
	private String forwardTo;
	
	@InjectMocks
	private SenderParseReceivedDataProcessorBodyFormData processor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		msg = UtilMessageService.getArtifactRequestMessage();
		headerAsString = UtilMessageService.getMessageAsString(msg);
		forwardTo = "https://forward.to.example";
	}

	@Test
	public void processBodyFormTest() throws Exception {
		mockExchangeGetHeaders(exchange);
		
		processor.process(exchange);
		
		MultipartMessage expectedMessage = new MultipartMessageBuilder()
				.withHeaderContent(msg)
				.withPayloadContent(PAYLOAD_STRING)
				.build();
		
		verify(message).setBody(expectedMessage);
	}
	
	@Test
	public void processBodyForm_PayloadFileTest() throws Exception {
		mockExchangeForFile(exchange);
		
		processor.process(exchange);
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(msg)
				.withPayloadContent(Base64.getEncoder().encodeToString(PAYLOAD_STRING.getBytes()))
				.withPayloadHeader(Map.of(
						Exchange.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
				.build();
		
		verify(message).setBody(multipartMessage);
	}
	
	@Test
	public void processBodyFormTestForwardTo_Null() throws Exception {
		forwardTo = null;
		mockExchangeGetHeaders(exchange);
		
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
		headers.put(Exchange.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
		headers.put("Forward-To", forwardTo);
		ByteArrayDataSource headerBarrds = new ByteArrayDataSource(headerAsString.getBytes(ContentType.APPLICATION_JSON.getCharset()), ContentType.APPLICATION_JSON.getMimeType());
		headers.put(MessagePart.HEADER, new DataHandler(headerBarrds));
		headers.put(MessagePart.PAYLOAD, PAYLOAD_STRING);
		when(message.getHeaders()).thenReturn(headers);
	}
	
	private void mockExchangeForFile(Exchange exchange) throws UnsupportedEncodingException {
		when(exchange.getMessage()).thenReturn(message);
		Map<String, Object> headers = new HashMap<>();
		headers.put(Exchange.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
		headers.put("Forward-To", forwardTo);
		ByteArrayDataSource headerBarrds = new ByteArrayDataSource(headerAsString.getBytes(ContentType.APPLICATION_JSON.getCharset()), ContentType.APPLICATION_JSON.getMimeType());
		headers.put(MessagePart.HEADER, new DataHandler(headerBarrds));
		ByteArrayDataSource payloadBarrds = new ByteArrayDataSource(PAYLOAD_STRING.getBytes(ContentType.APPLICATION_JSON.getCharset()), ContentType.APPLICATION_JSON.getMimeType());
		attachment.setHeader(Exchange.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
		when(message.getHeaders()).thenReturn(headers);
		when(exchange.getMessage(AttachmentMessage.class)).thenReturn(attachmentMessage);
		when(exchange.getMessage(AttachmentMessage.class).getAttachmentObject(MessagePart.PAYLOAD)).thenReturn(attachment);
		when(attachment.getHeaderNames()).thenReturn(Util.asList(Exchange.CONTENT_TYPE));
		when(attachment.getHeader(Exchange.CONTENT_TYPE)).thenReturn(ContentType.APPLICATION_JSON.getMimeType());
		when(attachment.getDataHandler()).thenReturn(new DataHandler(payloadBarrds));
	}
}
