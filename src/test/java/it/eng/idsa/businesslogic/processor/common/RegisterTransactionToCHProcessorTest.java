package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class RegisterTransactionToCHProcessorTest {
	
	@InjectMocks
	private RegisterTransactionToCHProcessor processor;
	
	@Mock
	private ClearingHouseService clearingHouseService;
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	@Mock
	private Exchange exchange;
	
	@Mock
	private org.apache.camel.Message camelMessage;
	
	@Mock
	private MultipartMessage multipartMessage;
	
	private Message requestMessage;
	
	private String payload;
	
	
	
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		requestMessage = UtilMessageService.getArtifactRequestMessage();
		when(exchange.getProperty("Original-Message-Header")).thenReturn(requestMessage);
		payload = "PAYLOAD";
	}
	
	@Test
	public void clearingHouseInteractionDisabled() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", false);
		
		processor.process(exchange);
		
		verify(clearingHouseService, times(0)).registerTransaction(requestMessage, payload);
	}
	
	@Test
	public void clearingHouseInteractionSuccesfull() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", true);
		mockExchangeHeaderAndBody();
		when(clearingHouseService.registerTransaction(requestMessage, payload)).thenReturn(true);
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(requestMessage, RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES);
		verify(clearingHouseService).registerTransaction(requestMessage, payload);
	}
	
	@Test
	public void clearingHouseInteractionFailed() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", true);
		mockExchangeHeaderAndBody();
		when(clearingHouseService.registerTransaction(requestMessage, payload)).thenReturn(false);
		
		processor.process(exchange);
		
		verify(rejectionMessageService).sendRejectionMessage(requestMessage, RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES);
		verify(clearingHouseService).registerTransaction(requestMessage, payload);
	}
	
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(requestMessage);
		when(multipartMessage.getPayloadContent()).thenReturn(payload);
	}

}
