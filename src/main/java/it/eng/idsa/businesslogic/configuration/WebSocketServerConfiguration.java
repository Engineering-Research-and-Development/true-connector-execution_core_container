package it.eng.idsa.businesslogic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import it.eng.idsa.businesslogic.processor.consumer.websocket.server.FileRecreatorBeanServer;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.FrameBufferBean;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.IdscpServerBean;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.InputStreamSocketListenerServer;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.RecreatedMultipartMessageBean;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.ResponseMessageSendPartialServer;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.FileStreamingBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.IdscpClientBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.InputStreamSocketListenerClient;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
public class WebSocketServerConfiguration {
	
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
