package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SenderClientService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RequestResponseUtil;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
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
	private MultipartMessageService multipartMessageService;
	@Mock
	private HttpHeaderService headerService;
	
	@Mock
	private SenderClientService okHttpClient;

	
	String header;
	
	String payload;
	
	Map<String, Object> headerParts;
	
	private MultipartMessage multipartMessage;
	
	private boolean eccCommunication = true;

	private String URL = "https://mock.address.com";
	private static final String RESPONSE_MESSAGE = "response message OK";
	
	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		payload = "{\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"}";
		headerParts = new HashMap<>();
		multipartMessage = createMultipartMessage();
	}
	
	private MultipartMessage createMultipartMessage() {
		return new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getArtifactRequestMessage())
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
				RESPONSE_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8("response payload"), 
				200);
		when(okHttpClient.sendMultipartMixRequest(URL, headers, mixRequestBody)).thenReturn(response);
		
		Response result = service.sendMessageBinary(URL, multipartMessage, headerParts, eccCommunication);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		
		verify(okHttpClient).createMultipartMixRequest(multipartMessage, MediaType.TEXT_PLAIN.toString());
	}
	
	@Test
	public void sendMessageFormSuccess() throws IOException {
		RequestBody formRequestBody = RequestResponseUtil.createRequestBody(payload); 
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, formRequestBody), 
				RESPONSE_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8("response payload"), 
				200);
		Headers headers = Headers.of(new HashMap<>());

		when(okHttpClient.createMultipartFormRequest(multipartMessage, MediaType.TEXT_PLAIN.toString()))
			.thenReturn(formRequestBody);
		when(okHttpClient.sendMultipartFormRequest(URL, headers, formRequestBody))
			.thenReturn(response);
		Response result = service.sendMessageFormData(URL, multipartMessage, headerParts, eccCommunication);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		
		verify(okHttpClient).createMultipartFormRequest(multipartMessage, MediaType.TEXT_PLAIN.toString());
	}

	@Test
	public void sendMessageHeaderSuccess() throws IOException {
		Map<String, Object> messageAsMap = getMessageAsMap();
		
		RequestBody headerRequestBody = RequestResponseUtil.createRequestBody(payload); 
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, headerRequestBody), 
				RESPONSE_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8("response payload"), 
				200);
		Headers headers = Headers.of(messageAsMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue())));
		
		when(headerService.prepareMessageForSendingAsHttpHeaders(multipartMessage)).thenReturn(messageAsMap);
		when(okHttpClient.sendHttpHeaderRequest(URL, headers, multipartMessage.getPayloadContent(), MediaType.TEXT_PLAIN.toString()))
			.thenReturn(response);
		
		Response result = service.sendMessageHttpHeader(URL, multipartMessage, headerParts, eccCommunication);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		
		verify(okHttpClient).sendHttpHeaderRequest(URL, headers, multipartMessage.getPayloadContent(), MediaType.TEXT_PLAIN.toString());
	}

	private Map<String, Object> getMessageAsMap() {
		Map<String, Object> messageAsMap = new HashMap<>();
		messageAsMap.put("IDS-Messagetype","ids:ArtifactResponseMessage");
		messageAsMap.put("IDS-Issued","2021-04-07T13:09:42.306Z");
		messageAsMap.put("IDS-IssuerConnector","http://true-connector.com");
		messageAsMap.put("IDS-Id","https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");
		messageAsMap.put("IDS-ModelVersion","4.0.0");
		messageAsMap.put("IDS-RequestedArtifact", "http:/true-connector/artifact/1");
		return messageAsMap;
	}
}
