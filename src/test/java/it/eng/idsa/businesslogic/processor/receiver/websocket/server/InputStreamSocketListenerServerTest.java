package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

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
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
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
		
		inputStreamSocketListenerServer.onMessage(session, InputStreamSocketListenerServer.END_BINARY_FRAME_SEPARATOR.getBytes(StandardCharsets.UTF_8));
		
		verify(frameBufferBean).add(InputStreamSocketListenerServer.END_BINARY_FRAME_SEPARATOR.getBytes(StandardCharsets.UTF_8));
		verify(responseMessageSendPartialServer).setup(any(Session.class));
	}
}
