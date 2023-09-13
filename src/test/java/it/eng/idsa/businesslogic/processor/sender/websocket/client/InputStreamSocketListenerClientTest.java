package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;

public class InputStreamSocketListenerClientTest {

	@InjectMocks
	private InputStreamSocketListenerClient client;
	
	@Mock
	private WebSocketClientConfiguration webSocketClientConfiguration;
	
	@Mock
	private ResponseMessageBufferClient bufferClient;
	
	private String payload = "PAYLOAD";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void onBinaryFrame() {
		when(webSocketClientConfiguration.responseMessageBufferWebSocketClient()).thenReturn(bufferClient);
		
		client.onBinaryFrame(payload.getBytes(), false, 0);
		
		verify(bufferClient).add(payload.getBytes());
	}
}
