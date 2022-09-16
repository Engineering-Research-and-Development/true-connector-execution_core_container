package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FrameBufferBeanTest {

	private FrameBufferBean frameBufferBean;
	public String message = "MESSAGE";
	
	@BeforeEach
	public void setup() {
		frameBufferBean = new FrameBufferBean();
	}
	
	@Test
	public void add() {
		frameBufferBean.add(message.getBytes());
	}
	
	@Test
	public void remove() {
		frameBufferBean.add(message.getBytes());
		var result = frameBufferBean.remove();
		assertNotNull(result);
		assertEquals(message, new String(result));
	}
}
