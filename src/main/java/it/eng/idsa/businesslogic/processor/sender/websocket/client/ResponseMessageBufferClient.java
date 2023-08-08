package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class ResponseMessageBufferClient {
	private static final Logger logger = LoggerFactory.getLogger(ResponseMessageBufferClient.class);

	private byte[] responseMessage = null;
	private boolean responseMessageIsReceived = false;
	
	public synchronized void add(byte[] msg) {
		if(responseMessageIsReceived) {
			try {
				wait();
			} catch(InterruptedException e) {
				logger.error("Adding to response message buffer interupted: {}", e.getMessage());
			}
		}
		
		this.responseMessage = msg;
		responseMessageIsReceived = true;
		notify();
	}
	
	public synchronized byte[] remove() {
		if(!responseMessageIsReceived) {
			try {
				wait();
			} catch(InterruptedException e) {
				logger.error("Removing from response message buffer interupted: {}", e.getMessage());
			}
		}
		
		responseMessageIsReceived = false;
		try {
			return responseMessage;
		}finally{
			notify();
			responseMessage = null;
		}
	}
}
