package it.eng.idsa.businesslogic.configuration; /* Created by ascatox on 15/04/2020 */

import it.eng.idsa.businesslogic.processor.receiver.websocket.server.*;

public interface WebSocketServerConfiguration {

    FrameBufferBean frameBufferWebSocket();
    HttpWebSocketServerBean httpsServerWebSocket();
    RecreatedMultipartMessageBean recreatedMultipartMessageBeanWebSocket();
    ResponseMessageBufferBean responseMessageBufferWebSocket();
    ResponseMessageSendPartialServer responseMessageSendPartialWebSocket();
    FileRecreatorBeanServer fileRecreatorBeanWebSocket();

}

