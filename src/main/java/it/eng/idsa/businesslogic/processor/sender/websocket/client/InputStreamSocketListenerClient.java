package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.springframework.beans.factory.annotation.Autowired;

import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class InputStreamSocketListenerClient implements WebSocketListener {

	@Autowired
	private WebSocketClientConfiguration webSocketClientConfiguration;
	
	@Override
	public void onOpen(WebSocket websocket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClose(WebSocket websocket, int code, String reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onBinaryFrame(byte[] payload, boolean finalFragment, int rsv) {
		webSocketClientConfiguration.responseMessageBufferWebSocketClient().add(payload);
	  }
}
