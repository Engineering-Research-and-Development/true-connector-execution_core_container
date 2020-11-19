package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.ws.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;

import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;

public class ResponseMessageReceiverClient implements Runnable{
	
    private static final Logger logger = LogManager.getLogger(ResponseMessageReceiverClient.class);

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
			logger.error(e);
		}
		
	}

}
