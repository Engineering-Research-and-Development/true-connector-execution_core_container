package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.websocket.api.Session;
import org.springframework.beans.factory.annotation.Autowired;

import de.fhg.aisec.ids.comm.server.IdscpServerSocket;
import de.fhg.aisec.ids.comm.server.SocketListener;
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class InputStreamSocketListenerServer implements SocketListener {
	
	private static final String CLOSURE_FRAME = "�normal closure";
	private static final String END_BINARY_FRAME_SEPARATOR = "�normal-IDS-ENG-SEPARATOR the-last-frame";
	
	private FrameBufferBean frameBuffer;
	
	@Autowired
	private WebSocketServerConfiguration webSocketServerConfiguration;
	
	public InputStreamSocketListenerServer() {
		
	}

	@Override
	public void onMessage(Session session, byte[] message) {
		
		String receivedMessage = new String(message, StandardCharsets.UTF_8);
		if(receivedMessage.equals(CLOSURE_FRAME)) {
			// The last frame is received - skip this frame
			// This indicate that Client WebSocket now is closed
		} else {
			// Put the received frame in the frameBuffer
			frameBuffer.add(message.clone());
			if (receivedMessage.equals(END_BINARY_FRAME_SEPARATOR)) {
				ResponseMessageSendPartialServer responseMessageSendPartialServer = webSocketServerConfiguration.responseMessageSendPartialWebSocket();
				responseMessageSendPartialServer.setup(session);
				Thread fileRecreatorBeanThread = new Thread(responseMessageSendPartialServer, "ResponseMessageSendPartialServer");
				fileRecreatorBeanThread.start();
			}
		}
	}

	@Override
	public void notifyClosed(IdscpServerSocket idscpServerSocket) {
		// TODO Auto-generated method stub
		
	}

	public void setFrameBuffer(FrameBufferBean frameBuffer) {
		this.frameBuffer = frameBuffer;
	}
	
}
