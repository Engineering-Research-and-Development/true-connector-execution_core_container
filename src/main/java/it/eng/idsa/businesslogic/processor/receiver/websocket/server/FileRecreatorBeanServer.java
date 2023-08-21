package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

public class FileRecreatorBeanServer implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(FileRecreatorBeanServer.class);

	private static final int DEFAULT_STREAM_BUFFER_SIZE = 127;
	private static final String END_BINARY_FRAME_SEPARATOR = "�normal-IDS-ENG-SEPARATOR the-last-frame";

	// @Autowired
	private WebSocketServerConfiguration webSocketServerConfiguration;

	private FrameBufferBean frameBuffer;
	private ArrayList<byte[]> fileByteArray = new ArrayList<byte[]>();
	private ByteBuffer byteBuffer = null;
	private RecreatedMultipartMessageBean recreatedmultipartMessage;

	public FileRecreatorBeanServer() {

	}

	public void setup() throws Exception {
		try {
			this.frameBuffer = webSocketServerConfiguration.frameBufferWebSocket();
			this.recreatedmultipartMessage = webSocketServerConfiguration.recreatedMultipartMessageBeanWebSocket();
			// TODO!!! ONLY for EndPoint B enables Idscp
			HttpWebSocketServerBean httpWebSocketServerBean = webSocketServerConfiguration.httpsServerWebSocket();
			httpWebSocketServerBean.createServer();
		} catch (Exception e) {
			logger.error("Could not start file recreator server: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	public void run() {
		receiveAllFrames();
		recreatedmultipartMessage.set(recreateMultipartMessageFromReceivedFrames());
	}

	private void receiveAllFrames() {
		boolean allFramesReceived = false;

		while (!allFramesReceived) {
			byte[] receivedFrame = this.frameBuffer.remove();

			try {
				if ((new String(receivedFrame, StandardCharsets.UTF_8)).equals(END_BINARY_FRAME_SEPARATOR)) {
					allFramesReceived = true;
					logger.info("Received the last frames: " + END_BINARY_FRAME_SEPARATOR);
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
		// fileByteArray(0) is our header of the File, which should not be included in
		// the byte[] of file
		for (int i = 1; i < fileByteArray.size(); i++) {
			byteBuffer.put(fileByteArray.get(i));
		}
		return byteBuffer.array();
	}

	private String recreateMultipartMessageFromReceivedFrames() {
		String multipartMessage = null;
		try {
			logger.info("Started process: Recreate the Multipart message from the received frames");
			multipartMessage = recreateMultipartMessage(this.fileByteArray);
			// once bytes are consumed to get message - empty array
			fileByteArray = new ArrayList<>();
			logger.info("Recreated the Multipart message from the received frames: lenght= {}", multipartMessage.length());
		} catch (IOException e) {
			logger.error("Coud not recreate the file from the received frames: {}", e.getMessage());
		}
		return multipartMessage;
	}

	private String recreateMultipartMessage(ArrayList<byte[]> fileByteArray) throws IOException {
		byte[] allFrames = getAllFrames(fileByteArray);
		String multipartMessage = new String(allFrames, StandardCharsets.UTF_8);
		return multipartMessage;
	}

	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	public void setByteBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}

	public void setWebSocketServerConfiguration(WebSocketServerConfiguration webSocketServerConfiguration) {
		this.webSocketServerConfiguration = webSocketServerConfiguration;
	}
}
