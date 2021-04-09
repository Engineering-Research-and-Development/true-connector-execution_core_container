package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


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
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class ContractAgreementProcessorTest {
	
	private static final String CONTRACT_AGREEMENT_MOCK = "CONTRACT_AGREEMENT_MOCK";

	@InjectMocks
	private ContractAgreementProcessor processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;
	@Mock
	private CommunicationService communicationService;
	
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
		ReflectionTestUtils.setField(processor, "usageControlDataAppURL", usageControlDataAppURL, String.class);
		ReflectionTestUtils.setField(processor, "addPolicyEndpoint", addPolicyEndpoint, String.class);
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", Boolean.TRUE, Boolean.class);
		contractAggreAgreementMessage = TestUtilMessageService.createContractAgreementMessage();
	}
	
	@Test
	public void verifyContractAgreement() throws Exception {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(contractAggreAgreementMessage);
		when(multipartMessage.getPayloadContent()).thenReturn(CONTRACT_AGREEMENT_MOCK);
		when(communicationService.sendDataAsJson(ucDataAppAddPolicyEndpoint, multipartMessage.getPayloadContent()))
			.thenReturn("response from UC dataApp when uplading policy");

		processor.process(exchange);
		
		verify(communicationService).sendDataAsJson(ucDataAppAddPolicyEndpoint, multipartMessage.getPayloadContent());
	}
	
	@Test
	public void verifyContractAgreementFailed_NoPayload() throws Exception {
		rejectionMessageService = new RejectionMessageServiceImpl();
		ReflectionTestUtils.setField(processor, "rejectionMessageService", 
				rejectionMessageService, RejectionMessageService.class);
		
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(contractAggreAgreementMessage);
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		verify(communicationService, times(0)).sendDataAsJson(ucDataAppAddPolicyEndpoint, multipartMessage.getPayloadContent());
	}
	
	@Test
	public void verifyContractAgreement_NotSent() throws Exception {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(TestUtilMessageService.getArtifactRequestMessage());

		processor.process(exchange);
		
		verify(communicationService, times(0)).sendDataAsJson(ucDataAppAddPolicyEndpoint, multipartMessage.getPayloadContent());
	}
}
