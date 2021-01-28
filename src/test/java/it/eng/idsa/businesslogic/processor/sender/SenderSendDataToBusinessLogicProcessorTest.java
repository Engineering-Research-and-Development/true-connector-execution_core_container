package it.eng.idsa.businesslogic.processor.sender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.service.impl.SendDataToBusinessLogicServiceImpl;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class SenderSendDataToBusinessLogicProcessorTest {

	@InjectMocks
	private SenderSendDataToBusinessLogicProcessor processor;
	
	@Mock
	private SendDataToBusinessLogicServiceImpl sendDataToBusinessLogicService;
	@Mock
	private MultipartMessageService multipartMessageService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private org.apache.camel.Message camelMessage;
	@Mock
	private MultipartMessage multipartMessage;

	private Map<String, Object> headers = new HashMap<>();
	private Message message;
	private static final String FORWARD_TO = "http://forward.to";
	private static final String PAYLOAD_RESPONSE = "payload response data";
	private static final String PAYLOAD = "payoad";

	private static final String HEADER_MESSAGE_STRING = "headerMessage";

	private Header[] responseHeaders;
	private RejectionMessageService rejectionMessageService;
	
	@Mock
	private CloseableHttpResponse response;
	@Mock
	private HttpEntity httpEntity;
	@Mock
	private InputStream is;
	@Mock
	private StatusLine statusLine;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		message = TestUtilMessageService.getArtifactRequestMessage();
		headers.put("Forward-To", FORWARD_TO);
		responseHeaders = new Header[] {
				new BasicHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()),
				new BasicHeader("fizz", "buzz")
			};
	}

	@Test
	public void sendHttpHeaderSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", "http-header", String.class);
		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(response);
		mockHandleResponsePart(200);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true);
		verify(camelMessage).setBody(PAYLOAD_RESPONSE);
	}
	
	@Test
	public void sendHttpHeaderNotFound() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", "http-header", String.class);
		rejectionMessageService = new RejectionMessageServiceImpl();
		ReflectionTestUtils.setField(processor, "rejectionMessageService", rejectionMessageService, RejectionMessageService.class);

		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(response);
		mockHandleResponsePart(404);
	
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true);
	}

	@Test
	public void sendHttpHeaderResponseNull() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", "http-header", String.class);
		rejectionMessageService = new RejectionMessageServiceImpl();
		ReflectionTestUtils.setField(processor, "rejectionMessageService", rejectionMessageService, RejectionMessageService.class);

		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(null);
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true);
	}
	
	@Test
	public void sendBodyBinarySuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", "mixed", String.class);
		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageBinary(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(response);
		mockHandleResponsePart(200);
		when(multipartMessageService.getHeaderContentString(PAYLOAD_RESPONSE)).thenReturn(HEADER_MESSAGE_STRING);
		when(multipartMessageService.getPayloadContent(PAYLOAD_RESPONSE)).thenReturn(PAYLOAD);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageBinary(FORWARD_TO, multipartMessage, headers, true);
		verify(camelMessage).setHeader("header", HEADER_MESSAGE_STRING);
		verify(camelMessage).setHeader("payload", PAYLOAD);
	}
	
	@Test
	public void sendFormSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", "form", String.class);
		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageFormData(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(response);
		mockHandleResponsePart(200);
		when(multipartMessageService.getHeaderContentString(PAYLOAD_RESPONSE)).thenReturn(HEADER_MESSAGE_STRING);
		when(multipartMessageService.getPayloadContent(PAYLOAD_RESPONSE)).thenReturn(PAYLOAD);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageFormData(FORWARD_TO, multipartMessage, headers, true);
		verify(camelMessage).setHeader("header", HEADER_MESSAGE_STRING);
		verify(camelMessage).setHeader("payload", PAYLOAD);
	}

	private void mockHandleResponsePart(int statusCode) throws IOException {
		when(response.getEntity()).thenReturn(httpEntity);
		when(httpEntity.getContent()).thenReturn(is);
		when(is.readAllBytes()).thenReturn(PAYLOAD_RESPONSE.getBytes());
		when(response.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(statusCode);
		when(response.getAllHeaders()).thenReturn(responseHeaders);
	}
	
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
	}
}