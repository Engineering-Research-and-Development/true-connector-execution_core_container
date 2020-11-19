package it.eng.idsa.businesslogic.configuration;
/* Created by ascatox on 15/04/2020 */

import it.eng.idsa.businesslogic.processor.receiver.websocket.server.IdscpServerBean;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.InputStreamSocketListenerServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class IdscpServerConfiguration {

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

}
