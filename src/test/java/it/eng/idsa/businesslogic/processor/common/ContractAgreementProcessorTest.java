package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ContractAgreementProcessorTest {
	
	@InjectMocks
	private ContractAgreementProcessor processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;
	@Mock
	private UsageControlService usageControlService;
	
	@Mock
	private MultipartMessage multipartMessage;
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	@Mock
	private ApplicationEventPublisher publisher;
	private ContractAgreementMessage contractAgreementMessage;
	private ContractAgreement contractAgreement;
	
	private String usageControlDataAppURL = "http://ucdata.app.mock";
	private String addPolicyEndpoint = "/addPolicy";
	
	String ucDataAppAddPolicyEndpoint = usageControlDataAppURL + addPolicyEndpoint;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", Boolean.TRUE, Boolean.class);
		contractAgreementMessage = UtilMessageService.getContractAgreementMessage();
		contractAgreement = UtilMessageService.getContractAgreement();
	}
	
	@Test
	public void verifyContractAgreement() throws Exception {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(exchange.getProperty("Original-Message-Header")).thenReturn(contractAgreementMessage);
		when(exchange.getProperty("Original-Message-Payload")).thenReturn(UtilMessageService.getMessageAsString(contractAgreement));
		when(multipartMessage.getHeaderContent()).thenReturn(createProcessNotificationMessage());
		when(usageControlService.uploadPolicy(any(String.class))).thenReturn("UPLOADED POLICY");

		processor.process(exchange);
		
		verify(usageControlService).uploadPolicy(any(String.class));
	}
	
	@Test
	public void verifyContractAgreement_NotUploaded() throws Exception {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(exchange.getProperty("Original-Message-Header")).thenReturn(contractAgreementMessage);
		when(exchange.getProperty("Original-Message-Payload")).thenReturn(UtilMessageService.getMessageAsString(contractAgreement));
		when(multipartMessage.getHeaderContent()).thenReturn(createProcessNotificationMessage());
		when(usageControlService.uploadPolicy(any(String.class))).thenReturn("UPLOADED POLICY");

		processor.process(exchange);
		
		verify(usageControlService).uploadPolicy(any(String.class));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(any(de.fraunhofer.iais.eis.Message.class), any(RejectionReason.class));
	}
	
	@Test
	public void verifyContractAgreement_NotSent() throws Exception {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(UtilMessageService.getArtifactRequestMessage());

		processor.process(exchange);

		verify(usageControlService, times(0)).uploadPolicy(any(String.class));
	}
	
	private MessageProcessedNotificationMessage createProcessNotificationMessage() {
		return new MessageProcessedNotificationMessageBuilder()
				._issued_(DateUtil.normalizedDateTime())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(URI.create("auto-generated"))
				._recipientConnector_(URI.create("auto-generated"))
				._correlationMessage_(URI.create("auto-generated"))
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._senderAgent_(URI.create("auto-generated"))
				.build();
	}	
}
