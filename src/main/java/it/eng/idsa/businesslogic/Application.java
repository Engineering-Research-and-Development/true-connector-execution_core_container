package it.eng.idsa.businesslogic;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import javax.jms.ConnectionFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * The App: market4.0 Execution Core Container Business Logic
 */
@EnableCaching
@SpringBootApplication
@EnableJms
public class Application {
	//HTTP port
	@Value("${http.port}")
	private int httpPort;
	
	public final static String QUEUE_INCOMING = "incoming";
	public final static String QUEUE_OUTCOMING = "outcoming";
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
		// Let's configure additional connector to enable support for both HTTP and HTTPS
		@Bean
		public ServletWebServerFactory servletContainer() {
			TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
			tomcat.addAdditionalTomcatConnectors(createStandardConnector());
			return tomcat;
		}

		private Connector createStandardConnector() {
			Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
			connector.setPort(httpPort);
			return connector;
		}
		
		@Bean
	    public JmsListenerContainerFactory<?> jmsFactory(ConnectionFactory connectionFactory,
	      DefaultJmsListenerContainerFactoryConfigurer configurer) {
	        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
	        // This provides all boot's default to this factory, including the message converter
	        configurer.configure(factory, connectionFactory);
	        return factory;
	    }
}
