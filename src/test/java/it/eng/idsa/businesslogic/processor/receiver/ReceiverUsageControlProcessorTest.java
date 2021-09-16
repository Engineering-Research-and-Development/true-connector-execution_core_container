package it.eng.idsa.businesslogic.processor.receiver;

import static org.junit.jupiter.api.Assertions.assertNull;
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

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.util.TestUtilMessageService;

public class ReceiverUsageControlProcessorTest {

	private static final String ORIGINAL_MESSAGE_HEADER = "Original-Message-Header";

	@InjectMocks
	private ReceiverUsageControlProcessor processor;

	@Mock
	private MultipartMessageService multipartMessageService;

	@Mock
	private RejectionMessageService rejectionMessageService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private  org.apache.camel.Message camelMessage;
	@Mock
	private  org.apache.camel.Message out;
	
	@Mock
	private Message message;
	@Mock
	private MultipartMessage multipartMessage;

	private ArtifactRequestMessage artifactRequestMessage;
	private ArtifactResponseMessage artifactResponseMessage;
	private DescriptionRequestMessage descriptionRequestMessage;
	private String originalMessageHeader;
	
	Map<String, Object> headers;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		artifactRequestMessage = TestUtilMessageService.getArtifactRequestMessage();
		artifactResponseMessage = TestUtilMessageService.getArtifactResponseMessage();
		descriptionRequestMessage = TestUtilMessageService.getDescriptionRequestMessage(null);
		originalMessageHeader = TestUtilMessageService.getMessageAsString(artifactRequestMessage);
		headers = new HashMap<>();
		headers.put(ORIGINAL_MESSAGE_HEADER, originalMessageHeader);
	}

	@Test
	public void payloadNullTest() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessageService.getMessage(originalMessageHeader)).thenReturn(artifactRequestMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(artifactResponseMessage);
		when(multipartMessage.getPayloadContent()).thenReturn(null);
		
		processor.process(exchange);

		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_USAGE_CONTROL, artifactRequestMessage);
	}
	
	
	@Test
	public void usageControlDisabled() {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", false);
		
		processor.process(exchange);
		
		verify(multipartMessageService, times(0)).getMessage(any());
		verify(rejectionMessageService, times(0)).sendRejectionMessage(any(), any());
	}
	
	@Test
	public void usageControlEnabledMessageNotArtifactResponseMessage() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessageService.getMessage(originalMessageHeader)).thenReturn(artifactRequestMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(descriptionRequestMessage);
		
		processor.process(exchange);
		
		verify(multipartMessageService).getMessage(originalMessageHeader);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_USAGE_CONTROL, artifactRequestMessage);
	}
	
	@Test
	public void usageControlEnabledMessageNotArtifactRequesteMessage() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessageService.getMessage(originalMessageHeader)).thenReturn(descriptionRequestMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(artifactResponseMessage);
		
		processor.process(exchange);
		
		verify(multipartMessageService).getMessage(originalMessageHeader);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_USAGE_CONTROL, artifactRequestMessage);
	}
	
	@Test
	public void usageControlSuccessfull() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessageService.getMessage(originalMessageHeader)).thenReturn(artifactRequestMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(artifactResponseMessage);
		when(multipartMessage.getPayloadContent()).thenReturn("mockPayload");
		
		processor.process(exchange);
		
		verify(multipartMessageService).getMessage(originalMessageHeader);
		assertNull(exchange.getMessage().getHeader(ORIGINAL_MESSAGE_HEADER));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_USAGE_CONTROL, artifactRequestMessage);
	}
}