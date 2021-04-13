package it.eng.idsa.businesslogic.util;

public enum RouterType {
	
	MULTIPART_MIX("mixed"),
	MULTIPART_BODY_FORM("form"),
	HTTP_HEADER("http-header");
	
	public final String label;

	private RouterType(String label) {
		this.label = label;
	}
	
}

