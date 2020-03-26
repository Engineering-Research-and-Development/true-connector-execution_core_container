package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import java.nio.charset.StandardCharsets;

/**
 * HttpWebSocketMessagingLogic will be responsible for parsing received data
 *
 * @author Antonio Scatoloni
 */
public class HttpWebSocketMessagingLogic {
    private static final Logger logger = LogManager.getLogger(HttpWebSocketMessagingLogic.class);
    private static final String CLOSURE_FRAME = "�normal closure";
    private static final String END_BINARY_FRAME_SEPARATOR = "�normal-IDS-ENG-SEPARATOR the-last-frame";

    private WebSocketServerConfiguration webSocketServerConfiguration;
    private static HttpWebSocketMessagingLogic instance;

    private HttpWebSocketMessagingLogic() {
    }

    public static HttpWebSocketMessagingLogic getInstance() {
        if (instance == null) {
            instance = new HttpWebSocketMessagingLogic();
        }
        return instance;
    }

    //Duplicate code fragment @See InputStreamSocketListenerServer onMessage()
    public void onMessage(Session session, byte[] message) {
        String receivedMessage = new String(message, StandardCharsets.UTF_8);
       if (receivedMessage.equals(CLOSURE_FRAME)) {
            // The last frame is received - skip this frame
            // This indicate that Client WebSocket now is closed
        } else {
            // Put the received frame in the frameBuffer
            webSocketServerConfiguration.frameBufferWebSocket().add(message.clone());
            if (receivedMessage.equals(END_BINARY_FRAME_SEPARATOR)) {
                ResponseMessageSendPartialServer responseMessageSendPartialServer = webSocketServerConfiguration.responseMessageSendPartialWebSocket();
                responseMessageSendPartialServer.setup(session);
                Thread fileRecreatorBeanThread = new Thread(responseMessageSendPartialServer, "ResponseMessageSendPartialServer");
                fileRecreatorBeanThread.start();
            }
           logger.info(HttpWebSocketMessagingLogic.class.getSimpleName() +" DATA DEQUEUED FROM SOCKET -> " + receivedMessage);

       }
    }

    public void setWebSocketServerConfiguration(WebSocketServerConfiguration webSocketServerConfiguration) {
        this.webSocketServerConfiguration = webSocketServerConfiguration;
    }
}