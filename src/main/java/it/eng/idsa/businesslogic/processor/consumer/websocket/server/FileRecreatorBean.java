package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.fhg.aisec.ids.comm.server.IdscpServer;
import it.eng.idsa.businesslogic.configuration.WebSocketConfiguration;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class FileRecreatorBean implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(FileRecreatorBean.class);
	
	private static final int DEFAULT_STREAM_BUFFER_SIZE = 127;
	// TODO: should fix these paths and file name
	private static final String FILE_PATH = "src\\main\\resources\\received-fiels\\";
	private static final String FILE_NAME = "Engineering-COPY.pdf";
	
	@Autowired
	private WebSocketConfiguration webSocketConfiguration;
	
	private FrameBufferBean frameBuffer;
	private InputStreamSocketListener inputStreamSocketListener;
	private IdscpServerBean idscpServer;
	private IdscpServer server;
	private ArrayList<byte[]> fileByteArray = new ArrayList<byte[]>();
	private ByteBuffer byteBuffer = null;
	
	public FileRecreatorBean() {
	}
	
	public void setup() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, URISyntaxException {
		this.frameBuffer = webSocketConfiguration.frameBufferWebSocket();
		this.inputStreamSocketListener = webSocketConfiguration.inputStreamSocketListenerWebSocket();
		this.inputStreamSocketListener.setFrameBuffer(this.frameBuffer);
		this.idscpServer = webSocketConfiguration.idscpServerWebSocket();
		this.idscpServer.setSocketListener(this.inputStreamSocketListener);
		this.server = this.idscpServer.createIdscpServer();
	}
	
	public void start() {
		new Thread(this, "FileRecreator").start();
	}
	

	@Override
	public void run() {
		receiveAllFrames();
		recreateMultipartMessageFromReceivedFrames();

//		// TODO: Adapt this for the multipart message
//		// Close the server
//	    try {
//	    	server.getServer().stop();
//	    } catch (Exception e) {
//	    	e.printStackTrace();
//		}
	}

	private void receiveAllFrames() {
		boolean allFramesReceived = false;
		
		while(!allFramesReceived) {
			byte[] receivedFrame = this.frameBuffer.remove();
			
			try {
				if((new String(receivedFrame, StandardCharsets.UTF_8)).equals("�normal closure")) {
					allFramesReceived = true;
					logger.info("Received the last frames: �normal closure");
				} else {
					this.fileByteArray.add(receivedFrame.clone());
				} 
			} finally {
				receivedFrame = null;
			}
		}
	}
	
	private byte[] getAllFrames(ArrayList<byte[]> fileByteArray) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_STREAM_BUFFER_SIZE * fileByteArray.size());
		// fileByteArray(0) is our header of the File, which should not be included in the byte[] of file
		for(int i=1; i<fileByteArray.size(); i++){
			byteBuffer.put(fileByteArray.get(i));
		}
		return byteBuffer.array();
	}

	private void recreateMultipartMessageFromReceivedFrames() {
		try {
			logger.info("Started process: Recreate the Multipart message from the received frames");
			String multipartMessage = recreateMultipartMessage(this.fileByteArray);
			logger.info("Recreated the Multipart message from the received frames:\n" + multipartMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error on the process of recreation the file from the received frames.");
		}
	}
	
	private String recreateMultipartMessage(ArrayList<byte[]> fileByteArray) throws IOException {
		byte[] allFrames = getAllFrames(fileByteArray);
		String multipartMessage = new String(allFrames, StandardCharsets.UTF_8);
		return multipartMessage;
	}

}
