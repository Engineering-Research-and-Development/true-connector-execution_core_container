package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class RegisterTransactionToCHProcessorTest {
	
	private RegisterTransactionToCHProcessor processor;
	
	@Mock
	private ClearingHouseConfiguration configuration;
	
	@Mock
	private Optional<ClearingHouseService> clearingHouseService;
	
	@Mock
	private ClearingHouseService chs;
	
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
		processor = new RegisterTransactionToCHProcessor(configuration, clearingHouseService, rejectionMessageService, false);
		requestMessage = UtilMessageService.getArtifactRequestMessage();
		when(exchange.getProperty("Original-Message-Header")).thenReturn(requestMessage);
		payload = "PAYLOAD";
		when(clearingHouseService.get()).thenReturn(chs);
	}
	
	@Test
	public void clearingHouseInteractionDisabled() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", false);
		
		processor.process(exchange);
		
		verify(clearingHouseService.get(), times(0)).registerTransaction(requestMessage, payload, requestMessage);
	}
	
	@Test
	public void clearingHouseInteractionSuccesfull() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", true);
		mockExchangeHeaderAndBody();
		when(clearingHouseService.get().registerTransaction(requestMessage, payload, requestMessage)).thenReturn(true);
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(requestMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		verify(clearingHouseService.get()).registerTransaction(requestMessage, payload, requestMessage);
	}
	
	@Test
	public void clearingHouseInteractionFailed() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", true);
		mockExchangeHeaderAndBody();
		when(clearingHouseService.get().registerTransaction(requestMessage, payload, requestMessage)).thenReturn(false);
		
		processor.process(exchange);
		
		verify(rejectionMessageService).sendRejectionMessage(requestMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		verify(clearingHouseService.get()).registerTransaction(requestMessage, payload, requestMessage);
	}
	
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(requestMessage);
		when(multipartMessage.getPayloadContent()).thenReturn(payload);
	}

}
