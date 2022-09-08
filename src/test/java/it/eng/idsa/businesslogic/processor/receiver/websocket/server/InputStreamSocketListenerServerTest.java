package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.any;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;

public class InputStreamSocketListenerServerTest {

	@InjectMocks
	private InputStreamSocketListenerServer inputStreamSocketListenerServer;
	@Mock
	private WebSocketServerConfigurationB webSocketServerConfiguration;
	@Mock
	private ResponseMessageSendPartialServer responseMessageSendPartialServer;
	@Mock
	private FrameBufferBean frameBufferBean;
	@Mock
	private Session session;
	
	private String message = "MESSAGE";
	public static final String END_BINARY_FRAME_SEPARATOR = "�normal-IDS-ENG-SEPARATOR the-last-frame";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		HttpWebSocketMessagingLogicA.getInstance().setWebSocketServerConfiguration(webSocketServerConfiguration);
	}
	
	@Test
	public void onMessage() {
		
		inputStreamSocketListenerServer.onMessage(session, message.getBytes());
		
		verify(frameBufferBean).add(message.getBytes());
	}
	
	@Test
	public void onMessage_end_frame_separator() {
		when(webSocketServerConfiguration.responseMessageSendPartialWebSocket()).thenReturn(responseMessageSendPartialServer);
		
		inputStreamSocketListenerServer.onMessage(session, END_BINARY_FRAME_SEPARATOR.getBytes(StandardCharsets.UTF_8));
		
		verify(frameBufferBean).add(END_BINARY_FRAME_SEPARATOR.getBytes(StandardCharsets.UTF_8));
		verify(responseMessageSendPartialServer).setup(any(Session.class));
	}
}
