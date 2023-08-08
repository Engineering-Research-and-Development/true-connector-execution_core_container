package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class ResponseMessageSendPartialServer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ResponseMessageSendPartialServer.class);

	
	private Session session;
	
	private static final int DEFAULT_STREAM_BUFFER_SIZE = 127;
	private int streamBufferSize = DEFAULT_STREAM_BUFFER_SIZE;
	private volatile int loopCounter;
	
	private WebSocketServerConfiguration webSocketServerConfiguration;

	public ResponseMessageSendPartialServer() {	
	}
	
	public void setup(Session session) {
		this.session = session;
	}
	
	@Override
	public void run() {
		ResponseMessageBufferBean responseMessageBuffer = webSocketServerConfiguration.responseMessageBufferWebSocket();
		RemoteEndpoint remote = session.getRemote();
		byte[] responseMessage = responseMessageBuffer.remove();
		try {
			sendResponseMessageAsPartialBytes(remote, responseMessage);
		} catch (IOException e) {
			logger.error("Error while sending response as partial bytes: {}", e.getMessage());
		}
	}
	
	private void sendResponseMessageAsPartialBytes(RemoteEndpoint remote, byte[] responseMessage) throws IOException {
		ArrayList<byte[]> devidedResponseMessage = divideResponseMessageArray(responseMessage, streamBufferSize);
	    for(int i=0; i<devidedResponseMessage.size(); i++) {
	    	ByteBuffer responseMessageStream = ByteBuffer.wrap(devidedResponseMessage.get(i));
	    	if(i==(devidedResponseMessage.size()-1)) {
	    		remote.sendPartialBytes(responseMessageStream, true);
	    	} else {
	    		remote.sendPartialBytes(responseMessageStream, false);
	    	}
	    }
	}
	
	private ArrayList<byte[]> divideResponseMessageArray(byte[] data, int blockSize){
		ArrayList<byte[]> dividedArray = new ArrayList<byte[]>();

		int blockCount = (data.length + blockSize - 1) / blockSize;

		byte[] range = null;
		
		for (loopCounter = 1; loopCounter < blockCount; loopCounter++) {
				int idx = (loopCounter - 1) * blockSize;
				range = Arrays.copyOfRange(data, idx, idx + blockSize);
				dividedArray.add(range);
		}

		// Last part
		int end = -1;
		if (data.length % blockSize == 0) {
				end = data.length;
		} else {
				end = data.length % blockSize + blockSize * (blockCount - 1);
		}
				
		range = Arrays.copyOfRange(data, (blockCount - 1) * blockSize, end);
		dividedArray.add(range);
		
		return dividedArray;
	}

	public void setWebSocketServerConfiguration(WebSocketServerConfiguration webSocketServerConfiguration) {
		this.webSocketServerConfiguration = webSocketServerConfiguration;
	}
}
