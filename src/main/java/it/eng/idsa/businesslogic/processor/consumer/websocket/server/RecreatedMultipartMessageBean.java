package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class RecreatedMultipartMessageBean {
	private String multipartMessage = null;
	private boolean multipartMessageIsRecreated = false;
	
	public synchronized void set(String multipartMessage) {
		if(multipartMessageIsRecreated) {
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
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
				e.printStackTrace();
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
