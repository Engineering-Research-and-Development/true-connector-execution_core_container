package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;

/**
 * HttpWebSocketMessagingLogic will be responsible for parsing received data
 *
 * @author Antonio Scatoloni
 */
public class HttpWebSocketMessagingLogicA {
    private static final Logger logger = LoggerFactory.getLogger(HttpWebSocketMessagingLogicA.class);
    private static final String FORWARD_TO_HEADER = "Forward-To:";

    private WebSocketServerConfiguration webSocketServerConfiguration;
    private static HttpWebSocketMessagingLogicA instance;

    private String forwardTo;

    private HttpWebSocketMessagingLogicA() {
    }

    public static HttpWebSocketMessagingLogicA getInstance() {
        if (instance == null) {
            instance = new HttpWebSocketMessagingLogicA();
        }
        return instance;
    }

    // TODO Duplicate code fragment @See InputStreamSocketListenerServer onMessage()
    public void onMessage(Session session, byte[] message) {
        String receivedMessage = new String(message, StandardCharsets.UTF_8);
        if(receivedMessage.contains(FORWARD_TO_HEADER)) {
              forwardTo = receivedMessage.substring(FORWARD_TO_HEADER.length());
        }
        else if (receivedMessage.equals(InputStreamSocketListenerServer.CLOSURE_FRAME)) {
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
       }
    }

    public void setWebSocketServerConfiguration(WebSocketServerConfiguration webSocketServerConfiguration) {
        this.webSocketServerConfiguration = webSocketServerConfiguration;
    }

    public String getForwardTo() {
        return forwardTo;
    }

}