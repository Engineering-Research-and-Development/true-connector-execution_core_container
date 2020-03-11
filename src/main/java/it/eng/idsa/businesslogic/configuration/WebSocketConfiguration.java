package it.eng.idsa.businesslogic.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

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
	public IdscpClientBean idscpClientServiceSinelton() {
		return new IdscpClientBean();
	}
	
	@Bean
	@Scope("singleton")
	public FileStreamingBean fileStreamingBeanWebSocket() {
		return new FileStreamingBean();
	}
	
}
