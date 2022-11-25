package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ClearingHouseServiceImplTest {
	
	@InjectMocks
	private ClearingHouseServiceImpl clearingHouseServiceImpl;
	
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
	
	private String mockEndpoint;
	
	private Message requestMessage;
	
	private Message contractAgreementMessage;
	
	private String contractAgreement;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		contractAgreementMessage = UtilMessageService.getContractAgreementMessage();
		contractAgreement  = UtilMessageService.getMessageAsString(UtilMessageService.getContractAgreement());
		requestMessage = UtilMessageService.getArtifactRequestMessageWithTransferContract(UtilMessageService.REQUESTED_ARTIFACT.toString(), "http://w3id.org/engrd/connector/examplecontract/fa17023f-3059-4b89-b3ae-c9cd4340ccd9");
		mockEndpoint = "https://clearinghouse.com";
		when(configuration.getBaseUrl()).thenReturn(mockEndpoint);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(UtilMessageService.getDynamicAttributeToken());
		when(selfDescriptionConfiguration.getConnectorURI()).thenReturn(URI.create("http://auto-generated"));
	}
	
//	  @Test
//	  public void testPIDCreationFail () {
//		  assertNull(clearingHouseServiceImpl.createProcessIdAtClearingHouse(contractAgreementMessage, getMessageProcessedNotificationMessage(), contractAgreement));
//	  }
	  
	  @Test
	  public void testRegisterArtifactMessageFail () throws UnsupportedEncodingException {
		  when(response.code()).thenReturn(404);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertFalse(clearingHouseServiceImpl.registerTransaction(requestMessage, null));
	  }
	  
	  @Test
	  public void testRegisterArtifactMessageSuccess () throws UnsupportedEncodingException{
		  when(configuration.getUsername()).thenReturn("username");
		  when(configuration.getPassword()).thenReturn("password");
		  when(response.code()).thenReturn(201);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertTrue(clearingHouseServiceImpl.registerTransaction(requestMessage, null));
	  }
	  
	  @Test
	  public void testCreateProcessIdAtClearingHouseSuccess () throws UnsupportedEncodingException  {
		  when(response.code()).thenReturn(201);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertNotNull(clearingHouseServiceImpl.createProcessIdAtClearingHouse(contractAgreementMessage, getMessageProcessedNotificationMessage(), contractAgreement));
	  }
	  
	  @Test
	  public void testCreateProcessIdAtClearingHouseFail () throws IOException  {
		  when(response.code()).thenReturn(404);
		  when(response.message()).thenReturn("failed");
		  when(response.body()).thenReturn(mock(ResponseBody.class));
		  when(response.body().string()).thenReturn("failed");
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertNull(clearingHouseServiceImpl.createProcessIdAtClearingHouse(contractAgreementMessage, getMessageProcessedNotificationMessage(), contractAgreement));
	  }
	  
	  @Test
	  public void testCreateProcessIdAtClearingHouseNoAgreement () throws IOException  {
		  when(response.code()).thenReturn(404);
		  when(response.message()).thenReturn("failed");
		  when(response.body()).thenReturn(mock(ResponseBody.class));
		  when(response.body().string()).thenReturn("failed");
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertNull(clearingHouseServiceImpl.createProcessIdAtClearingHouse(contractAgreementMessage, getMessageProcessedNotificationMessage(), null));
	  }
	  
	  @Test
	  public void testRegisterContractAgreementSuccess() throws UnsupportedEncodingException  {
		  when(response.code()).thenReturn(201);
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertTrue(clearingHouseServiceImpl.registerTransaction(contractAgreementMessage, contractAgreement));
	  }
	  
	  @Test
	  public void testRegisterContractAgreementFail() throws IOException  {
		  when(response.code()).thenReturn(404);
		  when(response.message()).thenReturn("failed");
		  when(response.body()).thenReturn(mock(ResponseBody.class));
		  when(response.body().string()).thenReturn("failed");
		  when(sendDataToBusinessLogicService.sendMessageFormData(any(), any(), any())).thenReturn(response);
		  assertFalse(clearingHouseServiceImpl.registerTransaction(contractAgreementMessage, null));
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
	  
	  
	  private Message getMessageProcessedNotificationMessage() {
			return new MessageProcessedNotificationMessageBuilder()
					._senderAgent_(contractAgreementMessage.getSenderAgent())
					._correlationMessage_(contractAgreementMessage.getCorrelationMessage())
					._issuerConnector_(contractAgreementMessage.getIssuerConnector())
					._securityToken_(contractAgreementMessage.getSecurityToken())
					._issued_(contractAgreementMessage.getIssued())
					._modelVersion_(contractAgreementMessage.getModelVersion())
					.build();
		}
	  
}
