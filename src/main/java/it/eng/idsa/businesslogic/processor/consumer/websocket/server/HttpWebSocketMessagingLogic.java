package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;

/**
 * HttpWebSocketMessagingLogic will be responsible for parsing received data
 *
 * @author Antonio Scatoloni
 */
public class HttpWebSocketMessagingLogic {
    private static final Logger logger = LogManager.getLogger(HttpWebSocketMessagingLogic.class);

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

    // TODO Duplicate code fragment @See InputStreamSocketListenerServer onMessage()
    public void onMessage(Session session, byte[] message) {
        String receivedMessage = new String(message, StandardCharsets.UTF_8);
       if (receivedMessage.equals(InputStreamSocketListenerServer.CLOSURE_FRAME)) {
            // The last frame is received - skip this frame
            // This indicate that Client WebSocket now is closed
        } else {
            // Put the received frame in the frameBuffer
            webSocketServerConfiguration.frameBufferWebSocket().add(message.clone());
            if (receivedMessage.equals(InputStreamSocketListenerServer.END_BINARY_FRAME_SEPARATOR)) {
                ResponseMessageSendPartialServer responseMessageSendPartialServer = webSocketServerConfiguration.responseMessageSendPartialWebSocket();
                responseMessageSendPartialServer.setup(session);
                Thread fileRecreatorBeanThread = new Thread(responseMessageSendPartialServer, "ResponseMessageSendPartialServer");
                fileRecreatorBeanThread.start();
            }
           logger.debug(HttpWebSocketMessagingLogic.class.getSimpleName() +" DATA RECEIVED FROM SOCKET -> " + receivedMessage);

       }
    }

    public void setWebSocketServerConfiguration(WebSocketServerConfiguration webSocketServerConfiguration) {
        this.webSocketServerConfiguration = webSocketServerConfiguration;
    }
}