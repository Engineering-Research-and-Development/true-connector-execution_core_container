package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.Helper;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class RegisterTransactionToCHProcessorTest {
	
	private RegisterTransactionToCHProcessor processor;
	
	@Mock
	private ClearingHouseConfiguration configuration;
	
	private Optional<ClearingHouseService> clearingHouseService;
	
	private Optional<UsageControlService> usageControlService;
	
	@Mock
	private ClearingHouseService chs;
	
	@Mock
	private UsageControlService ucs;
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	@Mock
	private Exchange exchange;
	
	@Mock
	private org.apache.camel.Message camelMessage;
	
	@Mock
	private MultipartMessage multipartMessage;
	
	@Mock
	private ApplicationEventPublisher publisher;
	
	private Message requestMessage;
	
	private Message contractAgreementMessage;
	
	private ContractAgreement contractAgreement;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		clearingHouseService = Optional.of(chs);
		usageControlService = Optional.of(ucs);
		processor = new RegisterTransactionToCHProcessor(configuration, clearingHouseService, usageControlService, rejectionMessageService, publisher, false, false);
		requestMessage = UtilMessageService.getArtifactRequestMessage();
		contractAgreementMessage = UtilMessageService.getContractAgreementMessage();
		contractAgreement  = UtilMessageService.getContractAgreement();
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", true);
		mockExchangeHeaderAndBody();
	}
	
	@Test
	public void clearingHouseInteractionDisabled() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", false);
		
		processor.process(exchange);
		
		verify(clearingHouseService.get(), times(0)).registerTransaction(requestMessage, null);
	}
	
	@Test
	public void clearingHouseInteractionSuccesfullArtifactMessage() throws Exception {
		when(multipartMessage.getHeaderContent()).thenReturn(requestMessage);
		when(chs.registerTransaction(any(), any())).thenReturn(true);
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(requestMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		verify(clearingHouseService.get()).registerTransaction(requestMessage, null);
	}
	
	@Test
	public void clearingHouseInteractionFailedArtifactMessage() throws Exception {
		when(multipartMessage.getHeaderContent()).thenReturn(requestMessage);
		when(chs.registerTransaction(any(), any())).thenReturn(false);
		
		when(exchange.getProperty("Original-Message-Header")).thenReturn(requestMessage);
		
		processor.process(exchange);
		
		verify(rejectionMessageService).sendRejectionMessage(requestMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		verify(clearingHouseService.get()).registerTransaction(requestMessage, null);
	}
	
	@Test
	public void clearingHouseInteractionSuccesfullContractAgreementMessage() throws Exception {
		when(multipartMessage.getHeaderContent()).thenReturn(getMessageProcessedNotificationMessage());
		when(chs.registerTransaction(any(), any())).thenReturn(true);
		
		when(exchange.getProperty("Original-Message-Header")).thenReturn(contractAgreementMessage);
		when(exchange.getProperty("Original-Message-Payload")).thenReturn(UtilMessageService.getMessageAsString(contractAgreement));
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(contractAgreementMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		verify(clearingHouseService.get()).registerTransaction(contractAgreementMessage, Helper.getUUID(contractAgreement.getId()));
	}
	
	@Test
	public void clearingHouseInteractionFailedContractAgreementMessage() throws Exception {
		when(multipartMessage.getHeaderContent()).thenReturn(getMessageProcessedNotificationMessage());
		when(chs.registerTransaction(any(), any())).thenReturn(false);
		
		when(exchange.getProperty("Original-Message-Header")).thenReturn(contractAgreementMessage);
		when(exchange.getProperty("Original-Message-Payload")).thenReturn(UtilMessageService.getMessageAsString(contractAgreement));
		
		processor.process(exchange);
		
		verify(rejectionMessageService).sendRejectionMessage(contractAgreementMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		verify(clearingHouseService.get()).registerTransaction(contractAgreementMessage, Helper.getUUID(contractAgreement.getId()));
	}
	
	@Test
	public void clearingHouseInteractionSuccesfullPIDCreation() throws Exception {
		ReflectionTestUtils.setField(processor, "isReceiver", true);
		when(multipartMessage.getHeaderContent()).thenReturn(getMessageProcessedNotificationMessage());
		when(chs.registerTransaction(any(), any())).thenReturn(true);
		when(chs.createProcessIdAtClearingHouse(any(), any())).thenReturn("somePid");
		
		when(exchange.getProperty("Original-Message-Header")).thenReturn(contractAgreementMessage);
		when(exchange.getProperty("Original-Message-Payload")).thenReturn(UtilMessageService.getMessageAsString(contractAgreement));
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(contractAgreementMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		verify(clearingHouseService.get()).registerTransaction(contractAgreementMessage, Helper.getUUID(contractAgreement.getId()));
		verify(clearingHouseService.get()).createProcessIdAtClearingHouse(contractAgreementMessage.getSecurityToken().getTokenValue(), Helper.getUUID(contractAgreement.getId()));
	}
	
	@Test
	public void clearingHouseInteractionPIDCreationFailed() throws Exception {
		ReflectionTestUtils.setField(processor, "isReceiver", true);
		when(multipartMessage.getHeaderContent()).thenReturn(getMessageProcessedNotificationMessage());
		when(chs.registerTransaction(any(), any())).thenReturn(true);
		when(chs.createProcessIdAtClearingHouse(any(), any())).thenReturn(null);
		
		when(exchange.getProperty("Original-Message-Header")).thenReturn(contractAgreementMessage);
		when(exchange.getProperty("Original-Message-Payload")).thenReturn(UtilMessageService.getMessageAsString(contractAgreement));
		
		processor.process(exchange);
		
		verify(rejectionMessageService).sendRejectionMessage(contractAgreementMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		verify(clearingHouseService.get(), times(0)).registerTransaction(contractAgreementMessage, Helper.getUUID(contractAgreement.getId()));
		verify(clearingHouseService.get()).createProcessIdAtClearingHouse(contractAgreementMessage.getSecurityToken().getTokenValue(), Helper.getUUID(contractAgreement.getId()));
		verify(usageControlService.get()).rollbackPolicyUpload(Helper.getUUID(contractAgreement.getId()));
	}
	
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
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
