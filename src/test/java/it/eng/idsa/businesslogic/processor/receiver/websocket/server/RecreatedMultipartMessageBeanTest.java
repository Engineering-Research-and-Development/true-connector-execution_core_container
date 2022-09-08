package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecreatedMultipartMessageBeanTest {

	private RecreatedMultipartMessageBean recreatedMultipartMessageBean;
	
	private String message = "MESSAGE";
	
	@BeforeEach
	public void setup() {
		recreatedMultipartMessageBean = new RecreatedMultipartMessageBean();
	}
	
	@Test
	public void add() {
		recreatedMultipartMessageBean.set(message);
	}
	
	@Test
	public void remove() {
		recreatedMultipartMessageBean.set(message);
		var result = recreatedMultipartMessageBean.remove();
		assertNotNull(result);
		assertEquals(message, new String(result));
	}
}
