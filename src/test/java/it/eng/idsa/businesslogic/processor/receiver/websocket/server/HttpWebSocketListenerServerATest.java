package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;

public class HttpWebSocketListenerServerATest {

	@InjectMocks
	private HttpWebSocketListenerServerA httpWebSocketListenerServerA;

	@Mock
	private ByteBuffer byteBuffer;
	@Mock
	private WebSocketServerConfigurationA webSocketServerConfiguration;
	@Mock
	private ResponseMessageSendPartialServer responseMessageSendPartialServer;
	@Mock
	private FrameBufferBean frameBufferBean;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(byteBuffer.remaining()).thenReturn(10);
		HttpWebSocketMessagingLogicA.getInstance().setWebSocketServerConfiguration(webSocketServerConfiguration);
	}
	
	@Test
	public void onWebSocketPartialBinary() throws Exception {
		when(webSocketServerConfiguration.responseMessageSendPartialWebSocket()).thenReturn(responseMessageSendPartialServer);
		when(webSocketServerConfiguration.frameBufferWebSocket()).thenReturn(frameBufferBean);
		httpWebSocketListenerServerA.onWebSocketPartialBinary(byteBuffer, false);
		
		byte[] arr = new byte[byteBuffer.remaining()];
		verify(frameBufferBean).add(arr);
	}

	@Test
	public void onWebSocketPartialText() {
		when(webSocketServerConfiguration.responseMessageSendPartialWebSocket()).thenReturn(responseMessageSendPartialServer);
		when(webSocketServerConfiguration.frameBufferWebSocket()).thenReturn(frameBufferBean);
		httpWebSocketListenerServerA.onWebSocketPartialBinary(byteBuffer, false);
		
		httpWebSocketListenerServerA.onWebSocketPartialText("MESSAGE", false);
		
		verify(frameBufferBean).add("MESSAGE".getBytes());
	}
}
