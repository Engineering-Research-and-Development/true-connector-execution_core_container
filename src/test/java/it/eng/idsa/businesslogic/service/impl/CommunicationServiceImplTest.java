package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class CommunicationServiceImplTest {

	@InjectMocks
	private CommunicationServiceImpl service;
	
	@Mock
	private RestTemplate restTemplate;
	
	private String endpoint = "http://endpoint.test";
	private String data = "DATA USED IN TEST";
	private String response = "RESPONSE DATA USED IN TEST";
	private ResponseEntity<String> responseEntity = ResponseEntity.ok("RESPONSE DATA USED IN TEST");
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void sendData() {
		when(restTemplate.postForObject(eq(endpoint), eq(data), eq(String.class))).thenThrow(RestClientException.class);
		
		var result = service.sendData(endpoint, data);
		
		assertNull(result);
	}
	
	@Test
	public void sendData_exception() {
		when(restTemplate.postForObject(eq(endpoint), eq(data), eq(String.class))).thenReturn(response);
		
		var result = service.sendData(endpoint, data);
		
		assertEquals(response, result);
	}
	
	@Test
	public void sendDataAsJson() {
		when(restTemplate.exchange(eq(endpoint), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
			.thenReturn(responseEntity);
		
		var result = service.sendDataAsJson(endpoint, data, ContentType.APPLICATION_JSON.getMimeType());
		
		assertNotNull(result);
	}
}
