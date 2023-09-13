package it.eng.idsa.businesslogic.processor.receiver;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.sender.websocket.client.MessageWebSocketOverHttpSender;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MultipartMessageUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ReceiverWebSocketSendDataToDataAppProcessorTest {

	@InjectMocks
	private ReceiverWebSocketSendDataToDataAppProcessor processor;
	
	private String openDataAppReceiver = "https://localhost/test/wss";
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	@Mock 
	private MultipartMessage multipartMessage;
	@Mock
	private MessageWebSocketOverHttpSender messageWebSocketOverHttpSender;
	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
	private ApplicationConfiguration configuration;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(processor, "openDataAppReceiver", openDataAppReceiver, String.class);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
	}
	
	@Test
	public void processSuccess() throws Exception {
		when(multipartMessage.getHeaderContentString()).thenReturn("HEADER_CONTENT");
		when(multipartMessage.getPayloadContent()).thenReturn("PAYLOAD");
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		when(messageWebSocketOverHttpSender
				 .sendMultipartMessageWebSocketOverHttps(any(String.class), any(Integer.class), any(String.class), any(String.class), any(String.class)))
		 	.thenReturn(MultipartMessageProcessor.multipartMessagetoString(multipartMessage));
		when(configuration.getOpenDataAppReceiver()).thenReturn(openDataAppReceiver);
		
		processor.process(exchange);
		
		verify(message).setBody(multipartMessage);
	}
	
	@Test
	public void processRejection() throws Exception {
		when(multipartMessage.getHeaderContentString()).thenReturn("HEADER_CONTENT");
		when(multipartMessage.getPayloadContent()).thenReturn("PAYLOAD");
		var originalMessage = UtilMessageService.getArtifactRequestMessage();
		when(exchange.getProperty("Original-Message-Header")).thenReturn(originalMessage);
		when(messageWebSocketOverHttpSender
				 .sendMultipartMessageWebSocketOverHttps(any(String.class), any(Integer.class), any(String.class), any(String.class), any(String.class)))
		 	.thenReturn(null);
		when(configuration.getOpenDataAppReceiver()).thenReturn("http://openDataAppReceiver");
		
		processor.process(exchange);
		
		verify(rejectionMessageService).sendRejectionMessage(originalMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
	}
}
