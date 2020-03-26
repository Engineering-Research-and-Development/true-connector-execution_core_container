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

//		RemoteEndpoint remote = session.getRemote();
//		// TODO: Wait while don't receive the response from the
//		String receivedMessage = new String(message, StandardCharsets.UTF_8);
//		// Blocking Send of a BINARY message to remote endpoint
//		// Part 1
//		ByteBuffer buf1 = ByteBuffer.wrap("djoka".getBytes());
//		// Part 2 (last part)
//		ByteBuffer buf2 = ByteBuffer.wrap(" iz potoka".getBytes());
//		if (receivedMessage.equals(CLOSURE_FRAME)) {
//			try {
//				remote.sendPartialBytes(buf1,false);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		    try {
//				remote.sendPartialBytes(buf2,true);// isLast is true
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
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
