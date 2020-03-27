package it.eng.idsa.businesslogic.configuration;

import it.eng.idsa.businesslogic.processor.consumer.websocket.server.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@Configuration
public class WebSocketServerConfiguration {

    private FrameBufferBean frameBufferBean;

    @Bean
    @Scope("singleton")
    public FrameBufferBean frameBufferWebSocket() {
       return new FrameBufferBean();
    }

    @Bean
    @Scope("singleton")
    public InputStreamSocketListenerServer inputStreamSocketListenerWebSocketServer() {
        return new InputStreamSocketListenerServer();
    }

    @Bean
    @Scope("singleton")
    public IdscpServerBean idscpServerWebSocket() {
        return new IdscpServerBean();
    }

    /**
     * @author Antonio Scatoloni
     * @return
     */
    @Bean
    @Scope("singleton")
    public HttpWebSocketServerBean httpsServerWebSocket() {
        return new HttpWebSocketServerBean();
    }
    /**
     * @author Antonio Scatoloni
     * @return
     */
    @Bean
    @Scope("singleton")
    public HttpWebSocketMessagingLogic messagingLogic() {
        HttpWebSocketMessagingLogic httpWebSocketMessagingLogic = HttpWebSocketMessagingLogic.getInstance();
        httpWebSocketMessagingLogic.setWebSocketServerConfiguration(this);
        return httpWebSocketMessagingLogic;
    }

    @Bean
    @Scope("singleton")
    public FileRecreatorBeanServer fileRecreatorBeanWebSocket() {
        return new FileRecreatorBeanServer();
    }

    @Bean
    @Scope("singleton")
    public RecreatedMultipartMessageBean recreatedMultipartMessageBeanWebSocket() {
        return new RecreatedMultipartMessageBean();
    }

    @Bean
    @Scope("singleton")
    public ResponseMessageBufferBean responseMessageBufferWebSocket() {
        return new ResponseMessageBufferBean();
    }

    @Bean
    @Scope("singleton")
    public ResponseMessageSendPartialServer responseMessageSendPartialWebSocket() {
        return new ResponseMessageSendPartialServer();
    }

}
