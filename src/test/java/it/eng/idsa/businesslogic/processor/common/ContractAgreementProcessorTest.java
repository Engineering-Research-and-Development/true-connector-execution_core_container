package it.eng.idsa.businesslogic.processor.common;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.ContractAgreementMessage;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.MockUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ContractAgreementProcessorTest {
	
	private static final String CONTRACT_AGREEMENT_MOCK = "CONTRACT_AGREEMENT_MOCK";

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

	private RejectionMessageService rejectionMessageService;
	private ContractAgreementMessage contractAggreAgreementMessage;
	
	private String usageControlDataAppURL = "http://ucdata.app.mock";
	private String addPolicyEndpoint = "/addPolicy";
	
	String ucDataAppAddPolicyEndpoint = usageControlDataAppURL + addPolicyEndpoint;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", Boolean.TRUE, Boolean.class);
		contractAggreAgreementMessage = UtilMessageService.getContractAgreementMessage();
	}
	
	@Test
	public void verifyContractAgreement() throws Exception {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(contractAggreAgreementMessage);
		when(multipartMessage.getPayloadContent()).thenReturn(CONTRACT_AGREEMENT_MOCK);
		when(usageControlService.uploadPolicy(any(String.class))).thenReturn("UPLOADED POLICY");

		processor.process(exchange);
		
		verify(usageControlService).uploadPolicy(any(String.class));
	}
	
	@Test
	public void verifyContractAgreementFailed_NoPayload() throws Exception {
		rejectionMessageService = MockUtil.mockRejectionService(rejectionMessageService);
		ReflectionTestUtils.setField(processor, "rejectionMessageService", 
				rejectionMessageService, RejectionMessageService.class);
		
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(contractAggreAgreementMessage);

		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		verify(usageControlService, times(0)).uploadPolicy(any(String.class));
	}
	
	@Test
	public void verifyContractAgreement_NotSent() throws Exception {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(UtilMessageService.getArtifactRequestMessage());

		processor.process(exchange);

		verify(usageControlService, times(0)).uploadPolicy(any(String.class));
	}
}
