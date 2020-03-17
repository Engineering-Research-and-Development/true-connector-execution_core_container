package it.eng.idsa.businesslogic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import it.eng.idsa.businesslogic.processor.consumer.websocket.server.FileRecreatorBean;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.FrameBufferBean;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.IdscpServerBean;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.InputStreamSocketListener;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.RecreatedMultipartMessageBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.FileStreamingBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.IdscpClientBean;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
public class WebSocketConfiguration {
	
	@Bean
	@Scope("singleton")
	public IdscpClientBean idscpClientServiceWebSocket() {
		return new IdscpClientBean();
	}
	
	@Bean
	@Scope("singleton")
	public FileStreamingBean fileStreamingWebSocket() {
		return new FileStreamingBean();
	}
	
	@Bean
	@Scope("singleton")
	public FrameBufferBean frameBufferWebSocket() {
		return new FrameBufferBean();
	}
	
	@Bean
	@Scope("singleton")
	public InputStreamSocketListener inputStreamSocketListenerWebSocket() {
		return new InputStreamSocketListener();
	}
	
	@Bean
	@Scope("singleton")
	public IdscpServerBean idscpServerWebSocket() {
		return new IdscpServerBean();
	}
	
	@Bean
	@Scope("singleton")
	public FileRecreatorBean fileRecreatorBeanWebSocket() {
		return new FileRecreatorBean();
	}

	@Bean
	@Scope("singleton")
	public RecreatedMultipartMessageBean recreatedMultipartMessageBeanWebSocket() {
		return new RecreatedMultipartMessageBean();
	}
}
