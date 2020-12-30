package it.eng.idsa.businesslogic.processor.receiver;

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
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class ReceiverUsageControlProcessorTest {

	private static final String ORIGINL_HEADER = "originalHeader";

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
	
	Map<String, Object> headers;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		headers = new HashMap<>();
		headers.put("Original-Message-Header", ORIGINL_HEADER);
		artifactRequestMessage = TestUtilMessageService.getArtifactRequestMessage();
		artifactResponseMessage = TestUtilMessageService.getArtifactResponseMessage();
		descriptionRequestMessage = TestUtilMessageService.getDescriptionRequestMessage();
	}

	@Test
	public void payloadNullTest() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessageService.getMessage(ORIGINL_HEADER)).thenReturn(artifactRequestMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(artifactResponseMessage);
		when(multipartMessage.getPayloadContent()).thenReturn(null);
		
//		when(exchange.getMessage()).thenReturn(out);
		processor.process(exchange);

		verify(rejectionMessageService).sendRejectionMessage(
                RejectionMessageType.REJECTION_USAGE_CONTROL,
                artifactRequestMessage);
	}
	
	@Test
	public void notUsageControlObject() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessageService.getMessage(ORIGINL_HEADER)).thenReturn(descriptionRequestMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(artifactResponseMessage);
//		when(exchange.getOut()).thenReturn(out);
		
		processor.process(exchange);
		
		verify(camelMessage, times(0)).setHeaders(headers);
		verify(camelMessage, times(0)).setBody(multipartMessage);
	}
}