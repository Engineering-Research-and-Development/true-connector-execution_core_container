package it.eng.idsa.businesslogic.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.service.HashFileService;

public class HashResourceTest {

	@InjectMocks
	private HashResource hashResource;
	
	@Mock
	private HashFileService hashService;
	
	private String forHashing = "STRING TO HASH";
	private String hashedValue = "HASHED STRING";
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void hashTest() throws Exception {
		when(hashService.getContent(forHashing)).thenReturn(hashedValue);
		String hashed = hashResource.getPayload(forHashing);
		
		assertEquals(hashedValue, hashed);
		verify(hashService).getContent(forHashing);
	}
}
