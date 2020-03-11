package it.eng.idsa.businesslogic.processor.producer.websocket.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import org.asynchttpclient.ws.WebSocket;

import de.fhg.aisec.ids.comm.client.IdscpClient;

public class FileStreamingBean {

	private static final int DEFAULT_STREAM_BUFFER_SIZE = 127;
	
	private int streamBufferSize = DEFAULT_STREAM_BUFFER_SIZE;
	
	public FileStreamingBean() {	
	}
	
	public void sendMultipartMessage(IdscpClient client, String multipartMessage, String serverIP, int serverPort) throws KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException, IOException {
		// Convert multipartMessage to the InputStream
		InputStream multipartMessageStream = new ByteArrayInputStream(multipartMessage.getBytes());
		
		// Try to connect to the Server. Wait until you are not connected to the server.
		WebSocket wsClient = client.connect(serverIP, serverPort);
		
		if(wsClient.isOpen()) {
			try {
				// Send multipartMessageStream as stream of the frames using the webSocket
				sendStreamMessage(wsClient, multipartMessageStream);
			} 
			finally
		    {
		        if (multipartMessageStream != null)
		        {
		        	multipartMessageStream.close();
		        }
		    }
		}
		
		// Send the frame 200 (OK), "Shutdown"
		try {
		   wsClient.sendCloseFrame(200, "Shutdown");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	// Streaming the frames
	private void sendStreamMessage(WebSocket webSocket, InputStream in) throws IOException {
	    byte[] readbuf = new byte[streamBufferSize];
	    byte[] writebuf = new byte[streamBufferSize];
	    int rn;
	    int wn = 0;
	    
	    // The first Frame should be BinaryFrame
	    webSocket.sendBinaryFrame("The initila Binary Frame.".getBytes(), false, 0);
	    
	    // Send content of the inputStream using the Frames
	    byte[] tempwritebuf=new byte[streamBufferSize];
		while ((rn = in.read(readbuf, 0, readbuf.length)) != -1) {
			if (wn > 0) {
				tempwritebuf=writebuf.clone();
				webSocket.sendContinuationFrame(tempwritebuf, false, 0);
			}
			System.arraycopy(readbuf, 0, writebuf, 0, rn);
			wn = rn;
		}
		
		// a bug in grizzly? we need to create a byte array with the exact length
		if (wn < writebuf.length) {
			byte[] tmpbuf = writebuf;
			tempwritebuf = new byte[wn]; 
			writebuf = new byte[wn];
			System.arraycopy(tmpbuf, 0, writebuf, 0, wn);
		} // ends
		
		// Send the last frame frame of the inputStream
		tempwritebuf=writebuf.clone();
		webSocket.sendContinuationFrame(tempwritebuf, true, 0);
	  }	
	
}
