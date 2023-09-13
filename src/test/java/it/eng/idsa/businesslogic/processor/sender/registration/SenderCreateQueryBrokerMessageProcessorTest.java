package it.eng.idsa.businesslogic.processor.sender.registration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessage;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.util.MultipartMessageUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SenderCreateQueryBrokerMessageProcessorTest {
	
	private String PAYLOAD_QUERY = "PAYLOAD_QUERY";

	@InjectMocks
	private SenderCreateQueryBrokerMessageProcessor processor;
	
	@Mock	
	private SelfDescriptionService selfDescriptionService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message camelMessage;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(exchange.getMessage()).thenReturn(camelMessage);
	}
	
	@Test
	public void process() throws Exception {
		when(camelMessage.getHeader("payload")).thenReturn(PAYLOAD_QUERY);
		QueryMessage queryMessage = UtilMessageService.getQueryMessage(UtilMessageService.SENDER_AGENT, 
				UtilMessageService.ISSUER_CONNECTOR, QueryLanguage.SPARQL);
		when(selfDescriptionService.getConnectorQueryMessage()).thenReturn(queryMessage);
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage(queryMessage, PAYLOAD_QUERY);
		
		processor.process(exchange);
		
		verify(camelMessage).setBody(multipartMessage);
	}
	
	@Test
	public void process_body() throws Exception {
		when(camelMessage.getBody(String.class)).thenReturn(PAYLOAD_QUERY);
		QueryMessage queryMessage = UtilMessageService.getQueryMessage(UtilMessageService.SENDER_AGENT, 
				UtilMessageService.ISSUER_CONNECTOR, QueryLanguage.SPARQL);
		when(selfDescriptionService.getConnectorQueryMessage()).thenReturn(queryMessage);
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage(queryMessage, PAYLOAD_QUERY);
		
		processor.process(exchange);
		
		verify(camelMessage).setBody(multipartMessage);
	}

}
