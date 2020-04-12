package it.eng.idsa.businesslogic.multipart;

public enum MultipartMessageKey {
	
	EMPTY(""),
	NAME_HEADER("header"),
	NAME_PAYLOAD("payload"),
	NAME_SIGNATURE("signature"),
	CONTENT_TYPE("Content-Type:"),
	CONTENT_DISPOSITION("Content-Disposition:"),
	CONTENT_LENGTH("Content-Length:");
 
    public final String label;
 
    private MultipartMessageKey(String label) {
        this.label = label;
    }
    
}
