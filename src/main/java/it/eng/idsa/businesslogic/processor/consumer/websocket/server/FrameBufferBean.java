package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class FrameBufferBean {
	private byte[] frame = null;
	private boolean frameIsReceived = false;
	
	public synchronized void add(byte[] msg) {
		if(frameIsReceived) {
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.frame = msg;
		frameIsReceived = true;
		notify();
	}
	
	public synchronized byte[] remove() {
		if(!frameIsReceived) {
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		frameIsReceived = false;
		notify();
		try {
			return frame;
		}finally{
			frame = null;
		}
	}
}
