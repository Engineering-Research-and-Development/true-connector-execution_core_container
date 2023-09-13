package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;

import it.eng.idsa.businesslogic.util.MultipartMessageUtil;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpSenderClientServiceImplTest {

	@InjectMocks
	private OkHttpSenderClientServiceImpl okHttpSenderClientServiceImpl;
	
	@Mock
	private OkHttpClient client;
	
	@Mock
	private Call call;
	
	@Mock
	private Response mockResponse;
	
	private String targetURL;
	
	@Mock
	private Headers httpHeaders;
	
	@Mock
	private RequestBody requestBody;
	
	@Mock
	private Request request;
	
	private String payload;
	
	private String contentType;
	
	
	@BeforeEach
	public void init () {
		MockitoAnnotations.openMocks(this);
		targetURL = "http://someUrl.com";
		httpHeaders = Headers.of("someKey", "someValue");
		payload = "somePayload";
	}
	
	
	@Test
	public void testSendMultipartMixRequest() throws IOException {
		
		when(client.newCall(any())).thenReturn(call);
		when(client.newCall(any()).execute()).thenReturn(mockResponse);
		
		Response response = okHttpSenderClientServiceImpl.sendMultipartMixRequest(targetURL, httpHeaders, requestBody);
		
		assertEquals(mockResponse, response);
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.sendMultipartMixRequest(targetURL, httpHeaders, requestBody));
		
	}
	
	@Test
	public void testSendMultipartFormRequest() throws IOException {
		
		when(client.newCall(any())).thenReturn(call);
		when(client.newCall(any()).execute()).thenReturn(mockResponse);
		
		Response response = okHttpSenderClientServiceImpl.sendMultipartFormRequest(targetURL, httpHeaders, requestBody);
		
		assertEquals(mockResponse, response);
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.sendMultipartFormRequest(targetURL, httpHeaders, requestBody));
		
	}
	
	@Test
	public void testSendHttpHeaderRequest_payload() throws IOException {
		
		contentType = javax.ws.rs.core.MediaType.TEXT_PLAIN;
		
		when(client.newCall(any())).thenReturn(call);
		when(client.newCall(any()).execute()).thenReturn(mockResponse);
		
		Response response = okHttpSenderClientServiceImpl.sendHttpHeaderRequest(targetURL, httpHeaders, payload, contentType);
		
		assertEquals(mockResponse, response);
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.sendHttpHeaderRequest(targetURL, httpHeaders, payload, contentType));
		
	}
	
	@Test
	public void testSendHttpHeaderRequest_noPayloadContentType() throws IOException {
		
		when(client.newCall(any())).thenReturn(call);
		when(client.newCall(any()).execute()).thenReturn(mockResponse);
		
		Response response = okHttpSenderClientServiceImpl.sendHttpHeaderRequest(targetURL, httpHeaders, payload, contentType);
		
		assertEquals(mockResponse, response);
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.sendHttpHeaderRequest(targetURL, httpHeaders, payload, contentType));
		
	}
	
	@Test
	public void testSendHttpHeaderRequest_withoutPayload() throws IOException {
		
		payload = null;
		
		when(client.newCall(any())).thenReturn(call);
		when(client.newCall(any()).execute()).thenReturn(mockResponse);
		
		Response response = okHttpSenderClientServiceImpl.sendHttpHeaderRequest(targetURL, httpHeaders, payload, contentType);
		
		assertEquals(mockResponse, response);
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.sendHttpHeaderRequest(targetURL, httpHeaders, payload, contentType));
		
	}
	
	@Test
	public void testCreateMultipartMixRequest() {
		
		contentType = javax.ws.rs.core.MediaType.TEXT_PLAIN;
		
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.createMultipartMixRequest(MultipartMessageUtil.getMultipartMessage(), contentType));
		
	}
	
	@Test
	public void testCreateMultipartMixRequest_NoPayload() {
		
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.createMultipartMixRequest(
				MultipartMessageUtil.getMultipartMessage(UtilMessageService.getArtifactRequestMessage(), null), contentType));
		
	}
	
	@Test
	public void testCreateMultipartFormRequest() {
		
		contentType = javax.ws.rs.core.MediaType.TEXT_PLAIN;
		
		Map<String, String> headers = new HashMap<>();
		
		headers.put(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"payload\"");
		
		MultipartMessage mm = new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getArtifactRequestMessage())
				.withPayloadContent(payload)
				.withPayloadHeader(headers)
				.build();
		
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.createMultipartFormRequest(mm, contentType));
		
	}
	
	@Test
	public void testCreateMultipartFormRequest_NoPayloadHeader() {
		
		contentType = javax.ws.rs.core.MediaType.TEXT_PLAIN;
		
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.createMultipartFormRequest(
				MultipartMessageUtil.getMultipartMessage(UtilMessageService.getArtifactRequestMessage(), payload), contentType));
		
	}
	
	@Test
	public void testCreateMultipartFormRequest_NoPayload() {
		
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.createMultipartFormRequest(
				MultipartMessageUtil.getMultipartMessage(UtilMessageService.getArtifactRequestMessage(), null), contentType));
		
	}
	
	@Test
	public void testsSndMultipartMixRequestPayload() throws IOException {
		
		when(client.newCall(any())).thenReturn(call);
		when(client.newCall(any()).execute()).thenReturn(mockResponse);
		
		Response response = okHttpSenderClientServiceImpl.sendMultipartMixRequestPayload(targetURL, httpHeaders, payload);
		
		assertEquals(mockResponse, response);
		assertDoesNotThrow(() -> okHttpSenderClientServiceImpl.sendMultipartMixRequestPayload(targetURL, httpHeaders, payload));
		
	}
}
