package it.eng.idsa.businesslogic.processor.producer.websocket.client;

import org.asynchttpclient.ws.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;

import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;

public class ResponseMessageReceiverClient implements Runnable{

	@Autowired
	private WebSocketClientConfiguration webSocketClientConfiguration;
	
	private WebSocket wsClient = null;
	
	public ResponseMessageReceiverClient() {
		
	}

	public void setup(WebSocket wsClient) {
		this.wsClient = wsClient;
	}
	
	@Override
	public void run() {
		byte[] responseMessage = webSocketClientConfiguration.responseMessageBufferWebSocketClient().remove();
		
		// Send the close frame 200 (OK), "Shutdown"; in this method we also close the wsClient.
		try {
		   wsClient.sendCloseFrame(200, "Shutdown");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
