package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResponseMessageBufferBeanTest {

	private ResponseMessageBufferBean bufferBean;
	public String message = "MESSAGE";
	
	@BeforeEach
	public void setup() {
		bufferBean = new ResponseMessageBufferBean();
	}
	
	@Test
	public void add() {
		bufferBean.add(message.getBytes());
	}
	
	@Test
	public void remove() {
		bufferBean.add(message.getBytes());
		var result = bufferBean.remove();
		assertNotNull(result);
		assertEquals(message, new String(result));
	}
}
