package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class FrameBufferBean {
	private static final Logger logger = LoggerFactory.getLogger(FrameBufferBean.class);

	private byte[] frame = null;
	private boolean frameIsReceived = false;
	
	public synchronized void add(byte[] msg) {
		if(frameIsReceived) {
			try {
				wait();
			} catch(InterruptedException e) {
				logger.error("Adding frame to buffer interupted: {}", e.getMessage());
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
				logger.error("Removing frame from buffer interupted: {}", e.getMessage());
			}
		}
		
		frameIsReceived = false;
		try {
			return frame;
		}finally{
			notify();
			frame = null;
		}
	}
}
