package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.HashFileService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.processor.util.TestUtilMessageService;

public class ClearingHouseServiceImplTest {
	
	@InjectMocks
	private ClearingHouseServiceImpl clearingHouseServiceImpl;
	
	private String payload;
	
	@Mock
	private HashFileService hashService;
	
	@Mock
	private DapsTokenProviderService dapsProvider;
	
	@Mock
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	
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
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(TestUtilMessageService.getDynamicAttributeToken());
		when(selfDescriptionConfiguration.getConnectorURI()).thenReturn(URI.create("http://auto-generated"));
		ReflectionTestUtils.setField(clearingHouseServiceImpl, "informationModelVersion", "4.1.1", String.class);
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
	  
	  @Test
	  @Disabled("Used to check new IM compatibility")
	  public void logNotificationMessage() {
		  Message logMessage = new LogMessageBuilder()
          ._modelVersion_("4.0.6")
          ._issued_(DateUtil.now())
          ._correlationMessage_(URI.create("https://correlationMessage"))
          ._issuerConnector_(URI.create("https://issuerConnector"))
          ._recipientConnector_(List.of(URI.create("https://recipient.connector")))
          ._senderAgent_(URI.create("https://sender.agent"))
          ._recipientAgent_(List.of(URI.create("https://recipient.agent")))
          ._transferContract_(null)
          ._securityToken_(null) //mandatory in SPECS but non suitable for Blockchain
          ._authorizationToken_(null)
          ._contentVersion_(null)
          .build();
		  assertNotNull(logMessage);
	  }
	  
}
