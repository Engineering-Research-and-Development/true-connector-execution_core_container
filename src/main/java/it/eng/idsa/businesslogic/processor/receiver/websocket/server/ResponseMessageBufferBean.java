package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class ResponseMessageBufferBean {
	
	private byte[] responseMessage = null;
	private boolean responseMessageIsReceived = false;
	
	public synchronized void add(byte[] msg) {
		if(responseMessageIsReceived) {
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
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
				e.printStackTrace();
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
