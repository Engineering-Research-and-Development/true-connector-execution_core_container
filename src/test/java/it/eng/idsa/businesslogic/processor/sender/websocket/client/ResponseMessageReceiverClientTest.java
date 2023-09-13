package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.asynchttpclient.ws.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;

public class ResponseMessageReceiverClientTest {

	@InjectMocks
	private ResponseMessageReceiverClient client;
	@Mock
	private WebSocketClientConfiguration webSocketClientConfiguration;
	@Mock
	private ResponseMessageBufferClient bufferClient;
	@Mock
	private WebSocket wsClient;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		client.setup(wsClient);
		when(webSocketClientConfiguration.responseMessageBufferWebSocketClient()).thenReturn(bufferClient);
		when(bufferClient.remove()).thenReturn("MESSAGE".getBytes());
	}
	
	@Test
	public void run() {
		client.run();
		
		verify(wsClient).sendCloseFrame(200, "Shutdown");
	}
}
