package it.eng.idsa.businesslogic.configuration;

import it.eng.idsa.businesslogic.processor.receiver.websocket.server.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@Configuration
@ConditionalOnProperty(
        value="application.dataApp.websocket.isEnabled",
        havingValue = "true",
        matchIfMissing = false)
public class WebSocketServerConfigurationA implements WebSocketServerConfiguration {

    @Value("${application.camelSenderPort}")
    private int port;

    @Override
    @Bean(name="frameBufferWebSocketA")
    @Scope("singleton")
    @Qualifier(value="FrameBufferBeanA")
    public FrameBufferBean frameBufferWebSocket() {
        return new FrameBufferBean();
    }

    @Override
    @Bean(name="httpsServerWebSocketA")
    @Scope("singleton")
    @Qualifier(value="HttpWebSocketServerBeanA")
    public HttpWebSocketServerBean httpsServerWebSocket() {
        HttpWebSocketServerBean httpWebSocketServerBean = new HttpWebSocketServerBean();
        httpWebSocketServerBean.setPort(port);
        httpWebSocketServerBean.setMessagingServlet(HttpWebSocketMessagingServletA.class);
        return httpWebSocketServerBean;
    }

    @Bean(name="messagingLogicA")
    @Scope("singleton")
    @Qualifier(value="MessagingLogicA")
    public HttpWebSocketMessagingLogicA messagingLogic() {
        HttpWebSocketMessagingLogicA httpWebSocketMessagingLogic = HttpWebSocketMessagingLogicA.getInstance();
        httpWebSocketMessagingLogic.setWebSocketServerConfiguration(this);
        return httpWebSocketMessagingLogic;
    }

    @Override
    @Bean(name="fileRecreatorBeanWebSocketA")
    @Scope("singleton")
    @Qualifier(value="FileRecreatorBeanServerA")
    public FileRecreatorBeanServer fileRecreatorBeanWebSocket() {
        FileRecreatorBeanServer fileRecreatorBeanServer = new FileRecreatorBeanServer();
        fileRecreatorBeanServer.setWebSocketServerConfiguration(this);
        return fileRecreatorBeanServer;
    }

    @Override
    @Bean(name="recreatedMultipartMessageBeanWebSocketA")
    @Scope("singleton")
    @Qualifier(value="RecreatedMultipartMessageBeanA")
    public RecreatedMultipartMessageBean recreatedMultipartMessageBeanWebSocket() {
        return new RecreatedMultipartMessageBean();
    }

    @Override
    @Bean(name="responseMessageBufferWebSocketA")
    @Scope("singleton")
    @Qualifier(value="ResponseMessageBufferBeanA")
    public ResponseMessageBufferBean responseMessageBufferWebSocket() {
        return new ResponseMessageBufferBean();
    }

    @Override
    @Bean(name="responseMessageSendPartialWebSocketA")
    @Scope("singleton")
    @Qualifier(value="ResponseMessageSendPartialServerA")
    public ResponseMessageSendPartialServer responseMessageSendPartialWebSocket() {
        ResponseMessageSendPartialServer responseMessageSendPartialServer = new ResponseMessageSendPartialServer();
        responseMessageSendPartialServer.setWebSocketServerConfiguration(this);
        return responseMessageSendPartialServer;
    }

}
