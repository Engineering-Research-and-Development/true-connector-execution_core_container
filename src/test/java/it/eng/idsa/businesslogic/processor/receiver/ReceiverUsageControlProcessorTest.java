package it.eng.idsa.businesslogic.processor.receiver;

import static org.mockito.Mockito.doThrow;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ReceiverUsageControlProcessorTest {

	private static final String ORIGINAL_MESSAGE_HEADER = "Original-Message-Header";

	@InjectMocks
	private ReceiverUsageControlProcessor processor;

	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
	private UsageControlService usageControlService;
	
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
	@Mock
	private ApplicationEventPublisher publisher;

	private ArtifactRequestMessage artifactRequestMessage;
	private ArtifactResponseMessage artifactResponseMessage;
	private DescriptionRequestMessage descriptionRequestMessage;
	private Message originalMessageHeader;
	
	Map<String, Object> headers;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		ReflectionTestUtils.setField(processor, "usageControlService", usageControlService);

		artifactRequestMessage = UtilMessageService.getArtifactRequestMessage();
		artifactResponseMessage = UtilMessageService.getArtifactResponseMessage();
		descriptionRequestMessage = UtilMessageService.getDescriptionRequestMessage(null);
		originalMessageHeader = artifactRequestMessage;
		headers = new HashMap<>();
		headers.put(ORIGINAL_MESSAGE_HEADER, originalMessageHeader);
	}

	@Test
	//Aplies only to MyData
	public void payloadNullTest() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessage.getHeaderContent()).thenReturn(artifactResponseMessage);
		when(multipartMessage.getPayloadContent()).thenReturn(null);
		when(exchange.getProperty("Original-Message-Header")).thenReturn(artifactRequestMessage);
		doThrow(JsonSyntaxException.class)
			.when(usageControlService).createUsageControlObject(artifactRequestMessage, artifactResponseMessage, null);
		processor.process(exchange);

		verify(rejectionMessageService).sendRejectionMessage(artifactRequestMessage, RejectionReason.NOT_AUTHORIZED);
	}
	
	@Test
	public void usageControlDisabled() {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", false);
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(artifactRequestMessage, RejectionReason.NOT_AUTHORIZED);
	}
	
	@Test
	public void usageControlEnabledMessageNotArtifactResponseMessage() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessage.getHeaderContent()).thenReturn(descriptionRequestMessage);
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(artifactRequestMessage, RejectionReason.NOT_AUTHORIZED);
	}
	
	@Test
	public void usageControlEnabledMessageNotArtifactRequesteMessage() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		headers.put(ORIGINAL_MESSAGE_HEADER, descriptionRequestMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessage.getHeaderContent()).thenReturn(artifactResponseMessage);
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(artifactRequestMessage, RejectionReason.NOT_AUTHORIZED);
	}
	
	@Test
	public void usageControlSuccessfull() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(usageControlService.createUsageControlObject(artifactRequestMessage, artifactResponseMessage, "mockPayload"))
			.thenReturn("meta data for usage control");
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessage.getHeaderContent()).thenReturn(artifactResponseMessage);
		when(multipartMessage.getPayloadContent()).thenReturn("mockPayload");
		
		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(artifactRequestMessage, RejectionReason.NOT_AUTHORIZED);
	}
}