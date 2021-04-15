package it.eng.idsa.businesslogic.processor.sender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.http.HttpEntity;
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
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RequestResponseUtil;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Request;
import okhttp3.Response;

public class SenderSendDataToBusinessLogicProcessorTest {

	@InjectMocks
	private SenderSendDataToBusinessLogicProcessor processor;
	
	@Mock
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
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

	private RejectionMessageService rejectionMessageService;

	@Mock
	private HttpEntity httpEntity;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		message = TestUtilMessageService.getArtifactRequestMessage();
		headers.put("Forward-To", FORWARD_TO);
	}

	@Test
	public void sendHttpHeaderSuccess() throws Exception {
		String jsonString = "aaaa";

		Request requestDaps =  RequestResponseUtil.createRequest(FORWARD_TO, 
				RequestResponseUtil.createRequestBody(jsonString)) ;
		Response response = RequestResponseUtil.createResponse(requestDaps, 
				"ABC", 
				RequestResponseUtil.createResponseBodyJsonUTF8(PAYLOAD_RESPONSE), 
				200);
		
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(response);
		doNothing().when(sendDataToBusinessLogicService).checkResponse(message, response, FORWARD_TO);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true);
		verify(camelMessage).setBody(PAYLOAD_RESPONSE);
	}
	
	@Test
	public void sendHttpHeaderNotFound() throws Exception {
		String jsonString = "aaaa";
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		rejectionMessageService = new RejectionMessageServiceImpl();
		ReflectionTestUtils.setField(processor, "rejectionMessageService", rejectionMessageService, RejectionMessageService.class);

		mockExchangeHeaderAndBody();
		
		Request requestDaps =  RequestResponseUtil.createRequest(FORWARD_TO, 
				RequestResponseUtil.createRequestBody(jsonString)) ;
		Response response = RequestResponseUtil.createResponse(requestDaps, 
				"ABC", 
				RequestResponseUtil.createResponseBodyJsonUTF8(PAYLOAD_RESPONSE), 
				404);
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(response);
		doThrow(ExceptionForProcessor.class).when(sendDataToBusinessLogicService).checkResponse(message, response, FORWARD_TO);
	
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true);
	}

	@Test
	public void sendHttpHeaderResponseNull() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		rejectionMessageService = new RejectionMessageServiceImpl();
		ReflectionTestUtils.setField(processor, "rejectionMessageService", rejectionMessageService, RejectionMessageService.class);

		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(null);
		doThrow(ExceptionForProcessor.class).when(sendDataToBusinessLogicService).checkResponse(message, null, FORWARD_TO);
	
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers, true);
	}
	
	@Test
	public void sendBodyBinarySuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_MIX, String.class);
		mockExchangeHeaderAndBody();
		
		String jsonString = "aaaa";
		Request requestDaps =  RequestResponseUtil.createRequest(FORWARD_TO, 
				RequestResponseUtil.createRequestBody(jsonString)) ;
		Response response = RequestResponseUtil.createResponse(requestDaps, 
				"ABC", 
				RequestResponseUtil.createResponseBodyJsonUTF8(PAYLOAD_RESPONSE), 
				200);
		
		when(sendDataToBusinessLogicService.sendMessageBinary(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(response);
		
		when(multipartMessageService.getHeaderContentString(PAYLOAD_RESPONSE)).thenReturn(HEADER_MESSAGE_STRING);
		when(multipartMessageService.getPayloadContent(PAYLOAD_RESPONSE)).thenReturn(PAYLOAD);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageBinary(FORWARD_TO, multipartMessage, headers, true);
		verify(camelMessage).setHeader(MessagePart.HEADER, HEADER_MESSAGE_STRING);
		verify(camelMessage).setHeader(MessagePart.PAYLOAD, PAYLOAD);
	}
	
	@Test
	public void sendFormSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_BODY_FORM, String.class);
		mockExchangeHeaderAndBody();
		
		String jsonString = "aaaa";
		Request requestDaps =  RequestResponseUtil.createRequest(FORWARD_TO, 
				RequestResponseUtil.createRequestBody(jsonString)) ;
		Response response = RequestResponseUtil.createResponse(requestDaps, 
				"ABC", 
				RequestResponseUtil.createResponseBodyJsonUTF8(PAYLOAD_RESPONSE), 
				200);
		when(sendDataToBusinessLogicService.sendMessageFormData(FORWARD_TO, multipartMessage, headers, true))
			.thenReturn(response);
		
		when(multipartMessageService.getHeaderContentString(PAYLOAD_RESPONSE)).thenReturn(HEADER_MESSAGE_STRING);
		when(multipartMessageService.getPayloadContent(PAYLOAD_RESPONSE)).thenReturn(PAYLOAD);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageFormData(FORWARD_TO, multipartMessage, headers, true);
		verify(camelMessage).setHeader(MessagePart.HEADER, HEADER_MESSAGE_STRING);
		verify(camelMessage).setHeader(MessagePart.PAYLOAD, PAYLOAD);
	}
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
	}
}