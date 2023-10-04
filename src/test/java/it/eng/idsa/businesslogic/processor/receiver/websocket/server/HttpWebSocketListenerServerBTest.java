package it.eng.idsa.businesslogic.processor.receiver.websocket.server;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;

public class HttpWebSocketListenerServerBTest {

	@InjectMocks
	private HttpWebSocketListenerServerB httpWebSocketListenerServerB;

	@Mock
	private ByteBuffer byteBuffer;
	@Mock
	private WebSocketServerConfigurationB webSocketServerConfiguration;
	@Mock
	private ResponseMessageSendPartialServer responseMessageSendPartialServer;
	@Mock
	private FrameBufferBean frameBufferBean;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(byteBuffer.remaining()).thenReturn(10);
		HttpWebSocketMessagingLogicB.getInstance().setWebSocketServerConfiguration(webSocketServerConfiguration);
	}
	
	@Test
	public void onWebSocketPartialBinary() throws Exception {
		when(webSocketServerConfiguration.responseMessageSendPartialWebSocket()).thenReturn(responseMessageSendPartialServer);
		when(webSocketServerConfiguration.frameBufferWebSocket()).thenReturn(frameBufferBean);
		httpWebSocketListenerServerB.onWebSocketPartialBinary(byteBuffer, false);
		
		byte[] arr = new byte[byteBuffer.remaining()];
		verify(frameBufferBean).add(arr);
	}

	@Test
	public void onWebSocketPartialText() {
		when(webSocketServerConfiguration.responseMessageSendPartialWebSocket()).thenReturn(responseMessageSendPartialServer);
		when(webSocketServerConfiguration.frameBufferWebSocket()).thenReturn(frameBufferBean);
		httpWebSocketListenerServerB.onWebSocketPartialBinary(byteBuffer, false);
		
		httpWebSocketListenerServerB.onWebSocketPartialText("MESSAGE", false);
		
		verify(frameBufferBean).add("MESSAGE".getBytes());
	}
}
