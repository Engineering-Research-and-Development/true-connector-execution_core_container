package it.eng.idsa.businesslogic.processor.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ExceptionProcessorReceiverTest {
	
	private String exceptionMessage = "EXCEPTION MESSAGE";

	@InjectMocks
	private ExceptionProcessorReceiver processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Exception exception;
	@Mock
	private Message message;
	
	@Mock
	private MultipartMessageService multipartMessageService;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT)).thenReturn(exception);
		when(exception.getMessage()).thenReturn(exceptionMessage);
		when(exchange.getMessage()).thenReturn(message);
		when(multipartMessageService.getHeaderContentString(exceptionMessage))
				.thenReturn(UtilMessageService.getMessageAsString(UtilMessageService.getRejectionMessage(RejectionReason.NOT_AUTHENTICATED)));
	}

	@Test
	public void processException() throws Exception {
		processor.process(exchange);
		
		verify(message).setBody(any(MultipartMessage.class));
	}
}
