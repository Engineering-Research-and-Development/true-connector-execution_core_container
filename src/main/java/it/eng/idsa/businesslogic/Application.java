package it.eng.idsa.businesslogic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

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
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
