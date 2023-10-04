package it.eng.idsa.businesslogic.processor.sender.registration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SenderCreateRegistrationMessageProcessorTest {

	@InjectMocks
	private SenderCreateRegistrationMessageProcessor processor;
	@Mock
	private SelfDescriptionService selfDescriptionService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;
	@Mock 
	private ConnectorUpdateMessage connMessage;
	
	@Mock
	private ApplicationEventPublisher publisher;
	
	ConnectorUpdateMessage connectorAvailable = UtilMessageService.getConnectorUpdateMessage(
			UtilMessageService.SENDER_AGENT, 
			UtilMessageService.ISSUER_CONNECTOR, 
			UtilMessageService.AFFECTED_CONNECTOR);

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(exchange.getMessage()).thenReturn(camelMessage);
	}
	
	@Test
	public void processRegistrationMessage() throws Exception {
		when(selfDescriptionService.getConnectorSelfDescription()).thenReturn("CONNECTOR_SELF_DESCRIPTION");
		when(selfDescriptionService.getConnectorAvailbilityMessage()).thenReturn(connectorAvailable);
		
		processor.process(exchange);
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(connectorAvailable)
				.withPayloadContent("CONNECTOR_SELF_DESCRIPTION")
				.build();
		
		verify(camelMessage).setBody(multipartMessage);
	}
	
	@Test
	public void getConnectorMessage() {
		when(selfDescriptionService.getConnectorAvailbilityMessage()).thenReturn(connectorAvailable);

		var message = processor.getConnectorMessage();
		
		assertTrue(message instanceof ConnectorUpdateMessage);
	}
}
