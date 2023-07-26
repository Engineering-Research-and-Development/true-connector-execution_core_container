package it.eng.idsa.businesslogic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * The App: TRUE Connector Execution Core Container Business Logic
 */
@EnableCaching
@SpringBootApplication
@EnableAsync
public class Application {
	
	public static void main(String[] args) {
		 System.setProperty("camel.springboot.main-run-controller", "true");
		 System.setProperty("camel.component.http4.use-global-ssl-context-parameters", "true");
		 System.setProperty("server.ssl.enabled", "true");
		 System.setProperty("camel.component.jetty.use-global-ssl-context-parameters", "true");
		SpringApplication.run(Application.class, args);
	}
}
