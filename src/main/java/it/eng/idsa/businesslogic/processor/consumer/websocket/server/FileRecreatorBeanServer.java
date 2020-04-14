package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.fhg.aisec.ids.comm.server.IdscpServer;
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

public class FileRecreatorBeanServer implements Runnable {

    private static final Logger logger = LogManager.getLogger(FileRecreatorBeanServer.class);

    private static final int DEFAULT_STREAM_BUFFER_SIZE = 127;
    private static final String END_BINARY_FRAME_SEPARATOR = "�normal-IDS-ENG-SEPARATOR the-last-frame";

    @Autowired
    private WebSocketServerConfiguration webSocketServerConfiguration;

    @Value("${application.idscp.isEnabled}")
    private boolean isEnabledIdscp;

    @Value("${application.websocket.isEnabled}")
    private boolean isEnabledWebSocket;


    private FrameBufferBean frameBuffer;
    private InputStreamSocketListenerServer inputStreamSocketListener;
    private IdscpServerBean idscpServer;
    private IdscpServer server;
    private ArrayList<byte[]> fileByteArray = new ArrayList<byte[]>();
    private ByteBuffer byteBuffer = null;
    private RecreatedMultipartMessageBean recreatedmultipartMessage;

    public FileRecreatorBeanServer() {

    }

    public void setup() throws Exception  {
    	try {
    		this.frameBuffer = webSocketServerConfiguration.frameBufferWebSocket();
    		this.recreatedmultipartMessage = webSocketServerConfiguration.recreatedMultipartMessageBeanWebSocket();
    		if (isEnabledIdscp) {
    			this.inputStreamSocketListener = webSocketServerConfiguration.inputStreamSocketListenerWebSocketServer();
    			this.inputStreamSocketListener.setFrameBuffer(this.frameBuffer);
    			this.idscpServer = webSocketServerConfiguration.idscpServerWebSocket();
    			this.idscpServer.setSocketListener(this.inputStreamSocketListener);
    			this.idscpServer.createIdscpServer();
    			this.setServer(this.idscpServer.getIdscpServer());
    		} else if (isEnabledWebSocket) {
    			HttpWebSocketServerBean httpWebSocketServerBean = webSocketServerConfiguration.httpsServerWebSocket();
    			httpWebSocketServerBean.createServer();
    		}
    	}catch (Exception e) {
    		e.printStackTrace();
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
        // fileByteArray(0) is our header of the File, which should not be included in the byte[] of file
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
            logger.info("Recreated the Multipart message from the received frames: lenght=" + multipartMessage.length());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Error on the process of recreation the file from the received frames.");
        }
        return multipartMessage;
    }

    private String recreateMultipartMessage(ArrayList<byte[]> fileByteArray) throws IOException {
        byte[] allFrames = getAllFrames(fileByteArray);
        String multipartMessage = new String(allFrames, StandardCharsets.UTF_8);
        return multipartMessage;
    }

	public IdscpServer getServer() {
		return server;
	}

	public void setServer(IdscpServer server) {
		this.server = server;
	}

	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	public void setByteBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}

}
