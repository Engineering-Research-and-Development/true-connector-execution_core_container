package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SenderClientService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RequestResponseUtil;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendDataToBusinessLogicServiceImplTest {
	
	@InjectMocks
	private SendDataToBusinessLogicServiceImpl service; 
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
	private HeaderCleaner headerCleaner;
	@Mock
	private HttpHeaderService headerService;
	
	@Mock
	private SenderClientService okHttpClient;
	
	String header;
	String payload;
	
	Map<String, Object> headerParts;
	
	private MultipartMessage multipartMessage;
	
	Map<String, Object> messageAsMap;
	
	private String URL = "https://mock.address.com";
	
	private static final String RESPONSE_SUCCESFULL_MESSAGE = "response message OK";
	
	private static final String RESPONSE_FAILED_MESSAGE = "bad request";
	
	private static final String RESPONSE_PAYLOAD = "response payload";
	
	@BeforeEach
	void init() {
		MockitoAnnotations.openMocks(this);
		payload = "{\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"}";
		headerParts = new HashMap<>();
		multipartMessage = createMultipartMessage();
		messageAsMap = getArtifactResponseMessageAsMap();
		ReflectionTestUtils.setField(service, "isEnabledIdscp2", Boolean.FALSE, Boolean.class);
	}
	
	private MultipartMessage createMultipartMessage() {
		return new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getArtifactRequestMessage())
				.withPayloadContent(payload)
				.build();
	}

	@Test
	void sendMessageBinarySuccess() throws IOException {
		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody(payload);
		when(okHttpClient.createMultipartMixRequest(multipartMessage, MediaType.TEXT_PLAIN.toString()))
			.thenReturn(mixRequestBody);
		Headers headers = Headers.of(new HashMap<>());
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				200);
		when(okHttpClient.sendMultipartMixRequest(URL, headers, mixRequestBody)).thenReturn(response);
		
		Response result = service.sendMessageBinary(URL, multipartMessage, headerParts);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		
		verify(okHttpClient).createMultipartMixRequest(multipartMessage, MediaType.TEXT_PLAIN.toString());
	}
	
	@Test
	void sendMessageBinarySuccess_idscpv2() throws IOException {
		ReflectionTestUtils.setField(service, "isEnabledIdscp2", Boolean.TRUE, Boolean.class);

		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody(payload);
		when(okHttpClient.createMultipartMixRequest(multipartMessage, MediaType.TEXT_PLAIN.toString()))
			.thenReturn(mixRequestBody);
		Map<String, String> h = new HashMap<>();
		h.put("idscp2", "idscp2");
		Headers headers = Headers.of(h);
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				200);
		when(okHttpClient.sendMultipartMixRequest(URL, headers, mixRequestBody)).thenReturn(response);
		
		Response result = service.sendMessageBinary(URL, multipartMessage, headerParts);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		
		verify(okHttpClient).createMultipartMixRequest(multipartMessage, MediaType.TEXT_PLAIN.toString());
		verify(headerCleaner).removeTechnicalHeaders(any(Map.class));
	}
	
	@Test
	void sendMessageBinaryFail() throws IOException {
		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody(payload);
		when(okHttpClient.createMultipartMixRequest(multipartMessage, MediaType.TEXT_PLAIN.toString()))
			.thenReturn(mixRequestBody);
		Headers headers = Headers.of(new HashMap<>());
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_FAILED_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				400);
		when(okHttpClient.sendMultipartMixRequest(URL, headers, mixRequestBody)).thenReturn(response);
		
		Response result = service.sendMessageBinary(URL, multipartMessage, headerParts);
		
		assertNotNull(result);
		assertEquals(result.code(), 400);
		
		verify(okHttpClient).createMultipartMixRequest(multipartMessage, MediaType.TEXT_PLAIN.toString());
	}
	
	@Test
	public void sendMessageFormSuccess() throws IOException {
		RequestBody formRequestBody = RequestResponseUtil.createRequestBody(payload); 
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, formRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				200);
		Headers headers = Headers.of(new HashMap<>());

		when(okHttpClient.createMultipartFormRequest(multipartMessage, MediaType.TEXT_PLAIN.toString()))
			.thenReturn(formRequestBody);
		when(okHttpClient.sendMultipartFormRequest(URL, headers, formRequestBody))
			.thenReturn(response);
		Response result = service.sendMessageFormData(URL, multipartMessage, headerParts);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		
		verify(okHttpClient).createMultipartFormRequest(multipartMessage, MediaType.TEXT_PLAIN.toString());
	}
	
	@Test
	public void sendMessageFormFail() throws IOException {
		RequestBody formRequestBody = RequestResponseUtil.createRequestBody(payload); 
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, formRequestBody), 
				RESPONSE_FAILED_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				400);
		Headers headers = Headers.of(new HashMap<>());

		when(okHttpClient.createMultipartFormRequest(multipartMessage, MediaType.TEXT_PLAIN.toString()))
			.thenReturn(formRequestBody);
		when(okHttpClient.sendMultipartFormRequest(URL, headers, formRequestBody))
			.thenReturn(response);
		Response result = service.sendMessageFormData(URL, multipartMessage, headerParts);
		
		assertNotNull(result);
		assertEquals(result.code(), 400);
		
		verify(okHttpClient).createMultipartFormRequest(multipartMessage, MediaType.TEXT_PLAIN.toString());
	}

	@Test
	public void sendMessageHeaderSuccess() throws IOException {
		RequestBody headerRequestBody = RequestResponseUtil.createRequestBody(payload); 
		
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, headerRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				200);
		Headers headers = Headers.of(messageAsMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue())));
		
		when(headerService.messageToHeaders(multipartMessage.getHeaderContent())).thenReturn(messageAsMap);
		when(okHttpClient.sendHttpHeaderRequest(URL, headers, multipartMessage.getPayloadContent(), MediaType.TEXT_PLAIN.toString()))
			.thenReturn(response);
		
		Response result = service.sendMessageHttpHeader(URL, multipartMessage, headerParts);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		
		verify(okHttpClient).sendHttpHeaderRequest(URL, headers, multipartMessage.getPayloadContent(), MediaType.TEXT_PLAIN.toString());
	}
	
	@Test
	public void sendMessageHeaderFail() throws IOException {
		RequestBody headerRequestBody = RequestResponseUtil.createRequestBody(payload); 
		
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, headerRequestBody), 
				RESPONSE_FAILED_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				400);
		Headers headers = Headers.of(messageAsMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue())));
		
		when(headerService.messageToHeaders(multipartMessage.getHeaderContent())).thenReturn(messageAsMap);
		when(okHttpClient.sendHttpHeaderRequest(URL, headers, multipartMessage.getPayloadContent(), MediaType.TEXT_PLAIN.toString()))
			.thenReturn(response);
		
		Response result = service.sendMessageHttpHeader(URL, multipartMessage, headerParts);
		
		assertNotNull(result);
		assertEquals(result.code(), 400);
		
		verify(okHttpClient).sendHttpHeaderRequest(URL, headers, multipartMessage.getPayloadContent(), MediaType.TEXT_PLAIN.toString());
	}
	
	@Test
	public void checkResponse_success() {
		Message messageForRejection = UtilMessageService.getArtifactRequestMessage();
		RequestBody headerRequestBody = RequestResponseUtil.createRequestBody(payload); 
		
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, headerRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				200);
		
		service.checkResponse(messageForRejection, response, "http://forward.to");
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(messageForRejection, RejectionReason.TEMPORARILY_NOT_AVAILABLE);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(messageForRejection, RejectionReason.BAD_PARAMETERS);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(messageForRejection, RejectionReason.INTERNAL_RECIPIENT_ERROR);
	}
	
	@Test
	public void checkResponse_fail() {
		Message messageForRejection = UtilMessageService.getArtifactRequestMessage();
		RequestBody headerRequestBody = RequestResponseUtil.createRequestBody(payload); 
		
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, headerRequestBody), 
				RESPONSE_FAILED_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				400);
		
		service.checkResponse(messageForRejection, response, "http://forward.to");
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(messageForRejection, RejectionReason.TEMPORARILY_NOT_AVAILABLE);
		verify(rejectionMessageService).sendRejectionMessage(messageForRejection, RejectionReason.INTERNAL_RECIPIENT_ERROR);
	}
	
	@Test
	public void checkResponse_notFound() {
		Message messageForRejection = UtilMessageService.getArtifactRequestMessage();
		RequestBody headerRequestBody = RequestResponseUtil.createRequestBody(payload); 
		
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, headerRequestBody), 
				RESPONSE_FAILED_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(RESPONSE_PAYLOAD), 
				404);
		
		service.checkResponse(messageForRejection, response, "http://forward.to");
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(messageForRejection, RejectionReason.TEMPORARILY_NOT_AVAILABLE);
		verify(rejectionMessageService).sendRejectionMessage(messageForRejection, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void checkResponse_response_null() {
		Message messageForRejection = UtilMessageService.getArtifactRequestMessage();
		
		doThrow(ExceptionForProcessor.class)
			.when(rejectionMessageService).sendRejectionMessage(messageForRejection, RejectionReason.TEMPORARILY_NOT_AVAILABLE);
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	service.checkResponse(messageForRejection, null, "http://forward.to");
	            });
		
		verify(rejectionMessageService).sendRejectionMessage(messageForRejection, RejectionReason.TEMPORARILY_NOT_AVAILABLE);
	}

	private Map<String, Object> getArtifactResponseMessageAsMap() {
		Map<String, Object> messageAsMap = new HashMap<>();
		messageAsMap.put("IDS-Messagetype","ids:ArtifactResponseMessage");
		messageAsMap.put("IDS-Issued","2021-04-07T13:09:42.306Z");
		messageAsMap.put("IDS-IssuerConnector","http://true-connector.com");
		messageAsMap.put("IDS-Id","https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");
		messageAsMap.put("IDS-ModelVersion","4.2.7");
		messageAsMap.put("IDS-RequestedArtifact", "http:/true-connector/artifact/1");
		return messageAsMap;
	}
}
