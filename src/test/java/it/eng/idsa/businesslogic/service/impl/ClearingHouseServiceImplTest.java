package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.HashFileService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
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
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	String mockEndpoint;
	
	Message message;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		message = TestUtilMessageService.getArtifactResponseMessage();
		payload = "{\"foo\":\"bar\"}";
		mockEndpoint = "https://clearinghouse.com";
		when(hashService.hash(payload)).thenReturn("ABC");
		when(configuration.getClearingHouseUrl()).thenReturn(mockEndpoint);
		when(restTemplate.postForObject(any(String.class), any(), any())).thenReturn(null);
	}
	
	  @Test
	  public void testRegisterTransactionFail () {
		  when(restTemplate.postForObject(any(String.class), any(), any())).thenThrow(new RestClientException("Service offline"));
		  assertFalse(clearingHouseServiceImpl.registerTransaction(message, payload));
		  verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, message);
	  }
	  
	  @Test
	  public void testRegisterTransactionSuccess ()  {
		 assertTrue(clearingHouseServiceImpl.registerTransaction(message, payload));
		 verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, message);
	  }
}
