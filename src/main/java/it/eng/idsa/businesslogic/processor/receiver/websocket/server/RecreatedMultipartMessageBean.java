package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class RecreatedMultipartMessageBean {
	private static final Logger logger = LoggerFactory.getLogger(RecreatedMultipartMessageBean.class);

	private String multipartMessage = null;
	private boolean multipartMessageIsRecreated = false;
	
	public synchronized void set(String multipartMessage) {
		if(multipartMessageIsRecreated) {
			try {
				wait();
			} catch(InterruptedException e) {
				logger.error("Failed to recreate multipart message: {}", e.getMessage());
			}
		}
		
		this.multipartMessage = multipartMessage;
		multipartMessageIsRecreated = true;
		notify();
	}
	
	public synchronized String remove() {
		if(!multipartMessageIsRecreated) {
			try {
				wait();
			} catch(InterruptedException e) {
				logger.error("Failed to remove recreated multipart message: {}", e.getMessage());
			}
		}
		
		multipartMessageIsRecreated = false;
		try {
			return multipartMessage;
		}finally{
			notify();
			multipartMessage = null;
		}
	}
}
