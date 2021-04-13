package it.eng.idsa.businesslogic.util;

public enum MessagePart {
	
	HEADER("header"),
	PAYLOAD("payload");
	
	public final String label;

	private MessagePart(String label) {
		this.label = label;
	}
	
}