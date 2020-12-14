package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.HashFileService;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;

public class ClearingHouseServiceImplTest {
	
	@InjectMocks
	private ClearingHouseServiceImpl clearingHouseServiceImpl;
	
	private String payload;
	
	@Mock
	private HashFileService hashService;
	
	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private ApplicationConfiguration configuration;
	
	String mockEndpoint;
	
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		payload = "{\"foo\":\"bar\"}";
		mockEndpoint = "https://clearinghouse.com";
		when(hashService.hash(payload)).thenReturn("ABC");
		when(configuration.getClearingHouseUrl()).thenReturn(mockEndpoint);
		when(restTemplate.postForObject(any(), any(), any())).thenReturn(null);
	}
	
	
	  @Test
	  @Disabled
	  //TODO enable test when CH is on the correct infomodel
	  public void testRegisterTransactionFail () {
		  assertThrows(ExceptionForProcessor.class, ()-> 
		  clearingHouseServiceImpl.registerTransaction(TestUtilMessageService.getArtifactResponseMessage(), payload));
	  }
	  
	  @Test
	  public void testRegisterTransactionSuccess () throws ConstraintViolationException, DatatypeConfigurationException {
		 assertTrue(clearingHouseServiceImpl.registerTransaction(TestUtilMessageService.getArtifactResponseMessage(), payload));
	  }
	  
	
	

}
