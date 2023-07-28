package it.eng.idsa.businesslogic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "camel.springboot.main-run-controller=true",
		"camel.component.http4.use-global-ssl-context-parameters=true",
		"camel.component.jetty.use-global-ssl-context-parameters=true", "server.ssl.enabled=true" })
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}

}
