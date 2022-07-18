package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ValidateTokenProcessorTest {

	private static final String TOKEN = UtilMessageService.TOKEN_VALUE;

	@InjectMocks
	private ValidateTokenProcessor processor;

	@Mock
	private DapsService dapsService;

	@Mock
	private Exchange exchange;
	@Mock
	private org.apache.camel.Message camelMessage;
	@Mock
	private MultipartMessage multipartMessage;
	@Mock
	private RejectionMessageService rejectionMessageService;
	private Map<String, Object> headers = new HashMap<>();
	private Message message;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidationDisabled() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", false);

		processor.process(exchange);

		verify(dapsService, times(0)).validateToken(any(String.class));
	}

	@Test
	public void validateTokenSuccess() throws Exception {
		message = UtilMessageService.getArtifactRequestMessage();
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", true);

		mockExchangeHeaderAndBody();

		when(multipartMessage.getHeaderContent()).thenReturn(message);
		when(dapsService.validateToken(TOKEN)).thenReturn(true);

		processor.process(exchange);

		verify(dapsService).validateToken(TOKEN);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, message);
	}

	@Test
	public void validateTokenFailed() throws Exception {
		message = UtilMessageService.getArtifactRequestMessage();
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", true);

		mockExchangeHeaderAndBody();

		when(multipartMessage.getToken()).thenReturn(TOKEN);
		when(dapsService.validateToken(TOKEN)).thenReturn(false);

		processor.process(exchange);

		verify(dapsService).validateToken(TOKEN);
		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, message);

	}
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
	}
}
