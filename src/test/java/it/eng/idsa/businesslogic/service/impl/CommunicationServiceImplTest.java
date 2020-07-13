package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

public class CommunicationServiceImplTest {
	
	@InjectMocks
	 CommunicationServiceImpl communicationServiceImpl;
	 String endpoint;
	 String data;
	 String expectedMessage;
	 @Mock
	 RestTemplate restTemplate;
	
	
	@Before
	public void init () {
		endpoint = "test.com";
		data = "test";
		expectedMessage = "test";
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testSendDataNotNull () {
		when(restTemplate.postForObject(endpoint, data, String.class)).thenReturn("test");
		String result = communicationServiceImpl.sendData(endpoint, data);
		assertEquals(expectedMessage, result, "The response is null or not as expected");
		
	}
	
	@Test
	public void testSendDataWhitNull () {
		
		when(restTemplate.postForObject(endpoint, data, String.class)).thenReturn(null);
		assertEquals(null, communicationServiceImpl.sendData(endpoint, data), "The response value should have been null");
		
	}

}
