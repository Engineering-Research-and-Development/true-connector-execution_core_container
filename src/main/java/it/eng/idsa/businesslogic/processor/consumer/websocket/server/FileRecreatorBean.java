package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhg.aisec.ids.comm.server.IdscpServer;
import it.eng.idsa.businesslogic.configuration.WebSocketConfiguration;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class FileRecreatorBean implements Runnable {
	
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
		boolean allFramesReceived = false;
		
		while(!allFramesReceived) {
			byte[] receivedFrame = this.frameBuffer.remove();
			
			try {
				if((new String(receivedFrame, StandardCharsets.UTF_8)).equals("�normal closure")) {
					allFramesReceived = true;
					System.out.println("Received the last frames");
				} else {
					this.fileByteArray.add(receivedFrame.clone());
				} 
			} finally {
				receivedFrame = null;
			}
		}
		
//		// TODO: Adapt this for the multipart message
//		// Convert byte[] to the File
//		try {
//			System.out.println("Started the proccess of the recreation the file from the received frames.");
//			recreateTheFile(this.fileByteArray);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("Error on the process of recreation the file from the received frames.");
//		}
//		
//		System.out.println("The file is recreated from the received frames.");
//		
//		// TODO: Adapt this for the multipart message
//		// Close the server
//	    try {
//	    	server.getServer().stop();
//	    } catch (Exception e) {
//	    	e.printStackTrace();
//		}
	}
	
	private byte[] getAllFrames(ArrayList<byte[]> fileByteArray) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_STREAM_BUFFER_SIZE * fileByteArray.size());
		// fileByteArray(0) is our header of the File, which should not be included in the byte[] of file
		for(int i=1; i<fileByteArray.size(); i++){
			byteBuffer.put(fileByteArray.get(i));
		}
		return byteBuffer.array();
	}
	
	private void recreateTheFile(ArrayList<byte[]> fileByteArray) throws IOException {
		byte[] allFrames = getAllFrames(fileByteArray);
		
		InputStream fileInputStream = new ByteArrayInputStream(allFrames);
		byte[] buffer = new byte[fileInputStream.available()];
		fileInputStream.read(buffer);
		
		File targetFile = new File(FILE_PATH + FILE_NAME);
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(allFrames);
	}

}
