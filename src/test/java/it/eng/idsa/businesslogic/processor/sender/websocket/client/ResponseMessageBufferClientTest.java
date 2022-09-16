package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResponseMessageBufferClientTest {

	private ResponseMessageBufferClient bufferClient;
	public String message = "MESSAGE";
	
	@BeforeEach
	public void setup() {
		bufferClient = new ResponseMessageBufferClient();
	}
	
	@Test
	public void add() {
		bufferClient.add(message.getBytes());
	}
	
	@Test
	public void remove() {
		bufferClient.add(message.getBytes());
		var result = bufferClient.remove();
		assertNotNull(result);
		assertEquals(message, new String(result));
	}
}
