package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Response;

public class ClearingHouseServiceImplTest {
	
	@InjectMocks
	private ClearingHouseServiceImpl clearingHouseServiceImpl;
	
	private String payload;
	
	@Mock
	private DapsTokenProviderService dapsProvider;
	
	@Mock
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	
	@Mock
	private ClearingHouseConfiguration configuration;
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	@Mock
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	
	@Mock
	private Response response;
	
	String mockEndpoint;
	
	Message message;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		message = UtilMessageService.getArtifactRequestMessageWithTransferContract(UtilMessageService.REQUESTED_ARTIFACT.toString(), "http://w3id.org/engrd/connector/examplecontract/fa17023f-3059-4b89-b3ae-c9cd4340ccd9");
		payload = "{\"foo\":\"bar\"}";
		mockEndpoint = "https://clearinghouse.com";
		when(configuration.getBaseUrl()).thenReturn(mockEndpoint);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(UtilMessageService.getDynamicAttributeToken());
		when(selfDescriptionConfiguration.getConnectorURI()).thenReturn(URI.create("http://auto-generated"));
	}
	
	  @Test
	  public void testRegisterTransactionFailNoPID () {
		  assertFalse(clearingHouseServiceImpl.registerTransaction(UtilMessageService.getArtifactRequestMessage(), payload, message));
	  }
	  
	  @Test
	  public void testRegisterTransactionFail () throws UnsupportedEncodingException {
		  when(response.code()).thenReturn(404);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertFalse(clearingHouseServiceImpl.registerTransaction(message, payload, message));
	  }
	  
	  @Test
	  public void testRegisterTransactionSuccess () throws UnsupportedEncodingException{
		  when(configuration.getUsername()).thenReturn("username");
		  when(configuration.getPassword()).thenReturn("password");
		  when(response.code()).thenReturn(201);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertTrue(clearingHouseServiceImpl.registerTransaction(message, payload, message));
	  }
	  
	  @Test
	  public void testCreateProcessIdAtClearingHouseSuccess () throws UnsupportedEncodingException  {
		  ReflectionTestUtils.setField(clearingHouseServiceImpl, "isReceiver", true);
		  when(response.code()).thenReturn(201);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertTrue(clearingHouseServiceImpl.registerTransaction(UtilMessageService.getContractAgreementMessage(), UtilMessageService.getMessageAsString(UtilMessageService.getContractAgreement()), message));
	  }
	  
	  @Test
	  public void testPIDfromContractAgreement() throws UnsupportedEncodingException  {
		  ReflectionTestUtils.setField(clearingHouseServiceImpl, "isReceiver", false);
		  when(response.code()).thenReturn(201);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertTrue(clearingHouseServiceImpl.registerTransaction(UtilMessageService.getContractAgreementMessage(), UtilMessageService.getMessageAsString(UtilMessageService.getContractAgreement()), message));
	  }
	  
	  @Test
	  public void testCreateProcessIdAtClearingHouseFail () throws UnsupportedEncodingException  {
		  ReflectionTestUtils.setField(clearingHouseServiceImpl, "isReceiver", true);
		  when(response.code()).thenReturn(404);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertFalse(clearingHouseServiceImpl.registerTransaction(UtilMessageService.getContractAgreementMessage(), UtilMessageService.getMessageAsString(UtilMessageService.getContractAgreement()), message));
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
