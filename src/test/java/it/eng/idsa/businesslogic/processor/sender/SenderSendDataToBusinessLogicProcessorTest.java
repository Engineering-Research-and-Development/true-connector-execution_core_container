package it.eng.idsa.businesslogic.processor.sender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
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
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.util.MockUtil;
import it.eng.idsa.businesslogic.util.OCSPValidation.OCSP_STATUS;
import it.eng.idsa.businesslogic.util.RequestResponseUtil;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public class SenderSendDataToBusinessLogicProcessorTest {

	@InjectMocks
	private SenderSendDataToBusinessLogicProcessor processor;
	
	@Mock
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	@Mock
	private HttpHeaderService httpHeaderService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private org.apache.camel.Message camelMessage;
	@Mock
	private MultipartMessage multipartMessage;

	private Map<String, Object> headers = new HashMap<>();
	private Headers okHeaders;
	private Message message;
	private static final String FORWARD_TO = "http://forward.to";
	private static final String PAYLOAD_RESPONSE = "payload response data";
	
	@Mock
	private RejectionMessageService rejectionMessageService;

	@Mock
	private HttpEntity httpEntity;

	private String multipartResponse;
	private Message artifactResponse;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		message = UtilMessageService.getArtifactRequestMessage();
		headers.put("Forward-To", FORWARD_TO);
		when(httpHeaderService.okHttpHeadersToMap(okHeaders)).thenReturn(headers);
		when(exchange.getProperty("Original-Message-Header")).thenReturn(UtilMessageService.getArtifactRequestMessage());
		artifactResponse = UtilMessageService.getArtifactResponseMessage();
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(artifactResponse)
				.withPayloadContent(PAYLOAD_RESPONSE)
				.build();
		multipartResponse = MultipartMessageProcessor.multipartMessagetoString(multipartMessage);
		ReflectionTestUtils.setField(processor, "desideredOCSPRevocationCheckValue", OCSP_STATUS.none, OCSP_STATUS.class);
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
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers))
			.thenReturn(response);
		
		when(httpHeaderService.headersToMessage(any(Map.class))).thenReturn(artifactResponse);
		doNothing().when(sendDataToBusinessLogicService).checkResponse(message, response, FORWARD_TO);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers);
		verify(camelMessage).setBody(any(MultipartMessage.class));
	}
	
	@Test
	public void sendHttpHeaderMissingHeader() throws Exception {
		String jsonString = "aaaa";

		Request requestDaps =  RequestResponseUtil.createRequest(FORWARD_TO, 
				RequestResponseUtil.createRequestBody(jsonString)) ;
		Response response = RequestResponseUtil.createResponse(requestDaps, 
				"ABC", 
				RequestResponseUtil.createResponseBodyJsonUTF8(PAYLOAD_RESPONSE), 
				200);
		
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers))
			.thenReturn(response);
		
		doNothing().when(sendDataToBusinessLogicService).checkResponse(message, response, FORWARD_TO);
		
		assertThrows(Exception.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers);
		verify(exchange.getMessage(), times(0)).setBody(any());
	}
	
	@Test
	public void sendHttpHeaderNotFound() throws Exception {
		String jsonString = "aaaa";
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		MockUtil.mockRejectionService(rejectionMessageService);
		
		mockExchangeHeaderAndBody();
		
		Request requestDaps =  RequestResponseUtil.createRequest(FORWARD_TO, 
				RequestResponseUtil.createRequestBody(jsonString)) ;
		Response response = RequestResponseUtil.createResponse(requestDaps, 
				"ABC", 
				RequestResponseUtil.createResponseBodyJsonUTF8(PAYLOAD_RESPONSE), 
				404);
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers))
			.thenReturn(response);
		doThrow(ExceptionForProcessor.class).when(sendDataToBusinessLogicService).checkResponse(message, response, FORWARD_TO);
	
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers);
		verify(exchange.getMessage(), times(0)).setBody(any());
	}

	@Test
	public void sendHttpHeaderResponseNull() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);
		MockUtil.mockRejectionService(rejectionMessageService);

		mockExchangeHeaderAndBody();
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers))
			.thenReturn(null);
		doThrow(ExceptionForProcessor.class).when(sendDataToBusinessLogicService).checkResponse(message, null, FORWARD_TO);
	
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(sendDataToBusinessLogicService).sendMessageHttpHeader(FORWARD_TO, multipartMessage, headers);
		verify(exchange.getMessage(), times(0)).setBody(any());
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
				RequestResponseUtil.createResponseBodyJsonUTF8(multipartResponse), 
				200);
		
		when(sendDataToBusinessLogicService.sendMessageBinary(FORWARD_TO, multipartMessage, headers))
			.thenReturn(response);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageBinary(FORWARD_TO, multipartMessage, headers);
	}
	
	@Test
	public void sendBodyBinaryFailed() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_MIX, String.class);
		mockExchangeHeaderAndBody();
		multipartResponse = multipartResponse.replace("Artifact", "something");
		
		String jsonString = "aaaa";
		Request requestDaps =  RequestResponseUtil.createRequest(FORWARD_TO, 
				RequestResponseUtil.createRequestBody(jsonString)) ;
		Response response = RequestResponseUtil.createResponse(requestDaps, 
				"ABC", 
				RequestResponseUtil.createResponseBodyJsonUTF8(multipartResponse), 
				200);
		
		when(sendDataToBusinessLogicService.sendMessageBinary(FORWARD_TO, multipartMessage, headers))
			.thenReturn(response);
		
		assertThrows(Exception.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		
		verify(exchange.getMessage(), times(0)).setBody(any());
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
				RequestResponseUtil.createResponseBodyJsonUTF8(multipartResponse), 
				200);
		when(sendDataToBusinessLogicService.sendMessageFormData(FORWARD_TO, multipartMessage, headers))
			.thenReturn(response);
		
		processor.process(exchange);
		
		verify(sendDataToBusinessLogicService).sendMessageFormData(FORWARD_TO, multipartMessage, headers);
	}
	
	@Test
	public void sendFormFailed() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.MULTIPART_BODY_FORM, String.class);
		mockExchangeHeaderAndBody();
		multipartResponse = multipartResponse.replace("Artifact", "something");
		
		String jsonString = "aaaa";
		Request requestDaps =  RequestResponseUtil.createRequest(FORWARD_TO, 
				RequestResponseUtil.createRequestBody(jsonString)) ;
		Response response = RequestResponseUtil.createResponse(requestDaps, 
				"ABC", 
				RequestResponseUtil.createResponseBodyJsonUTF8(multipartResponse), 
				200);
		when(sendDataToBusinessLogicService.sendMessageFormData(FORWARD_TO, multipartMessage, headers))
			.thenReturn(response);
		
		assertThrows(Exception.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(sendDataToBusinessLogicService).sendMessageFormData(FORWARD_TO, multipartMessage, headers);
	}
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
	}
}