package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import org.eclipse.jetty.websocket.api.Session;

import de.fhg.aisec.ids.comm.server.IdscpServerSocket;
import de.fhg.aisec.ids.comm.server.SocketListener;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class InputStreamSocketListener implements SocketListener {
	
	private FrameBufferBean frameBuffer;
	
	public InputStreamSocketListener() {
		
	}

	@Override
	public void onMessage(Session session, byte[] message) {
		// TODO Auto-generated method stub
		frameBuffer.add(message.clone());
	}

	@Override
	public void notifyClosed(IdscpServerSocket idscpServerSocket) {
		// TODO Auto-generated method stub
		
	}

	public void setFrameBuffer(FrameBufferBean frameBuffer) {
		this.frameBuffer = frameBuffer;
	}
	
}
