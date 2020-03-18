package it.eng.idsa.businesslogic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import it.eng.idsa.businesslogic.processor.producer.websocket.client.FileStreamingBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.IdscpClientBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.InputStreamSocketListenerClient;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
public class WebSocketClientConfiguration {

	// Client
	@Bean
	@Scope("singleton")
	public IdscpClientBean idscpClientServiceWebSocket() {
		return new IdscpClientBean();
	}
	
	// Client
	@Bean
	@Scope("singleton")
	public FileStreamingBean fileStreamingWebSocket() {
		return new FileStreamingBean();
	}
	
	// Client
	@Bean
	@Scope("singleton")
	public InputStreamSocketListenerClient inputStreamSocketListenerWebSocketClient() {
		return new InputStreamSocketListenerClient();
	}
}
