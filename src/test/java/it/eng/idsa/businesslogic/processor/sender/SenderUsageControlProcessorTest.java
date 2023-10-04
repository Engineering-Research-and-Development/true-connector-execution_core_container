package it.eng.idsa.businesslogic.processor.sender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
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

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.MockUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SenderUsageControlProcessorTest {

	@InjectMocks
	private SenderUsageControlProcessor processor;

	@Mock
	private UsageControlService ucService;
	@Mock
	private HeaderCleaner headerCleaner;
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
	@Mock
	private ArtifactRequestMessage artifactRequestMessage;
	private Map<String, Object> headers;

	private Message message;

	private Message requestMessage;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		message = UtilMessageService.getArtifactResponseMessage();
		requestMessage = UtilMessageService.getArtifactRequestMessage();
		when(exchange.getProperty("Original-Message-Header")).thenReturn(requestMessage);
		headers = new HashMap<>();
	}

	@Test
	public void usageControlDisabled() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", false);
		processor.process(exchange);
		verify(ucService, times(0)).enforceUsageControl(any(URI.class), any(URI.class), any(String.class));
	}

	@Test
	public void usageControlEnabledAndAllowed() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		mockExchangeHeaderAndBody();
		when(multipartMessage.getPayloadContent()).thenReturn(mockUsageControlPayload());
		when(ucService.enforceUsageControl(any(URI.class), any(URI.class), any(String.class)))
				.thenReturn("Usage allowed");
		when(exchange.getProperty("Original-Message-Header", ArtifactRequestMessage.class))
				.thenReturn(artifactRequestMessage);
		processor.process(exchange);

		verify(ucService).enforceUsageControl(any(URI.class), any(), any(String.class));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(requestMessage, RejectionReason.NOT_AUTHORIZED);
	}

	@Test
	public void usageControlEnabledAndInhibited() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		rejectionMessageService = MockUtil.mockRejectionService(rejectionMessageService);
		ReflectionTestUtils.setField(processor, "rejectionMessageService", rejectionMessageService,
				RejectionMessageService.class);

		mockExchangeHeaderAndBody();
		when(multipartMessage.getPayloadContent()).thenReturn(mockUsageControlPayload());
		when(ucService.enforceUsageControl(any(URI.class), any(URI.class), any(String.class))).thenReturn(null);
		when(exchange.getProperty("Original-Message-Header", ArtifactRequestMessage.class))
				.thenReturn(artifactRequestMessage);
		doThrow(Exception.class).when(ucService).enforceUsageControl(any(URI.class), any(), any(String.class));

		assertThrows(ExceptionForProcessor.class, () -> {
			processor.process(exchange);
		});

		verify(ucService).enforceUsageControl(any(URI.class), any(), any(String.class));
	}

	@Test
	public void usageControlEnabledAndNotUCObject() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		mockExchangeHeaderAndBody();
		when(multipartMessage.getPayloadContent()).thenReturn("not UC payload");
		when(exchange.getProperty("Original-Message-Header", ArtifactRequestMessage.class))
				.thenReturn(artifactRequestMessage);
		doThrow(NullPointerException.class).when(ucService).enforceUsageControl(any(URI.class), any(),
				any(String.class));

		processor.process(exchange);

		verify(ucService).enforceUsageControl(any(URI.class), any(), any(String.class));
		verify(rejectionMessageService).sendRejectionMessage(requestMessage, RejectionReason.NOT_AUTHORIZED);
	}

	@Test
	public void usageControlEnabledAndPayloadNull() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		mockExchangeHeaderAndBody();
		when(multipartMessage.getPayloadContent()).thenReturn(null);
		when(ucService.enforceUsageControl(any(), any(), any())).thenThrow(new NullPointerException());
		when(exchange.getProperty("Original-Message-Header", ArtifactRequestMessage.class))
				.thenReturn(artifactRequestMessage);
		processor.process(exchange);

		verify(ucService).enforceUsageControl(any(URI.class), any(), any());
		verify(rejectionMessageService).sendRejectionMessage(requestMessage, RejectionReason.NOT_AUTHORIZED);
	}

	@Test
	public void usageControlEnabledMessageNotArtifactResponseMessage() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		message = UtilMessageService.getRejectionMessage(RejectionReason.NOT_AUTHORIZED);
		mockExchangeHeaderAndBody();

		processor.process(exchange);

		verify(ucService, times(0)).enforceUsageControl(any(URI.class), any(URI.class), any(String.class));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(requestMessage, RejectionReason.NOT_AUTHORIZED);
	}

	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
	}

	private String mockUsageControlPayload() {
		return "{\"payload\":\"{\\\"firstName\\\":\\\"John\\\",\\\"lastName\\\":\\\"Doe\\\",\\\"address\\\":\\\"591  Franklin Street, Pennsylvania\\\",\\\"checksum\\\":\\\"ABC123 2022/06/22 10:22:39\\\",\\\"dateOfBirth\\\":\\\"2022/06/22 10:22:39\\\"}\",\"targetArtifactId\":\"http://w3id.org/engrd/connector/artifact/23\",\"assignee\":\"https://consumer.com\",\"assigner\":\"https://provider.com\"}";
	}
}