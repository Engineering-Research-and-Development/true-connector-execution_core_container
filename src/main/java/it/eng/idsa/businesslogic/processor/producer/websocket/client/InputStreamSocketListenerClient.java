package it.eng.idsa.businesslogic.processor.producer.websocket.client;

import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class InputStreamSocketListenerClient implements WebSocketListener {

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
    public void onTextFrame(String payload, boolean finalFragment, int rsv) {
		//TODO: There should handle the 
      	System.out.println(payload);
    }
	
}
