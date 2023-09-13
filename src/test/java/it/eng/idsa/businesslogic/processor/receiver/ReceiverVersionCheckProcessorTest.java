package it.eng.idsa.businesslogic.processor.receiver;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration.SelfDescription;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class ReceiverVersionCheckProcessorTest {

	private static final String INBOUND_MODEL_VERSION = "4.0.0,4.1.0,4.1.2,4.2.0,4.2.1,4.2.2,4.2.3,4.2.4,4.2.5,4.2.6,4.2.7";
	private static final String SUPPORTED_INFO_MODEL ="4.2.7";
	private static final String NOT_SUPPORTED_INFO_MODEL ="3.0.0";

	@InjectMocks
	private ReceiverVersionCheckProcessor processor;
	@Mock
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
	private SelfDescription selfDescription;
	
	@Mock
	private Exchange exchange;
	@Mock
	private  org.apache.camel.Message camelMessage;
	@Mock
	private MultipartMessage multipartMessage;
	@Mock
	private Message idsMessage;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(idsMessage);

		when(selfDescriptionConfiguration.getSelfDescription()).thenReturn(selfDescription);
		when(selfDescription.getInboundModelVersion()).thenReturn(INBOUND_MODEL_VERSION);
	}
	
	@Test
	public void versionSupported() throws Exception {
		when(idsMessage.getModelVersion()).thenReturn(SUPPORTED_INFO_MODEL);

		processor.process(exchange);
		
		verify(rejectionMessageService, times(0)).sendRejectionMessage(idsMessage, RejectionReason.VERSION_NOT_SUPPORTED);
	}
	
	@Test
	public void versionNotSupproted() throws Exception {
		when(idsMessage.getModelVersion()).thenReturn(NOT_SUPPORTED_INFO_MODEL);

		processor.process(exchange);
		
		verify(rejectionMessageService).sendRejectionMessage(idsMessage, RejectionReason.VERSION_NOT_SUPPORTED);
	}
}
