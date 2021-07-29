package it.eng.idsa.businesslogic.service.resources;

/**
 * Exception used to wrap Json related exceptions
 * @author igor.balog
 *
 */
public class JsonException extends RuntimeException {

	private static final long serialVersionUID = -3890600552655076624L;

	public JsonException(String message) {
		super(message);
	}
	
	public JsonException(String message, Exception e) {
		super(message, e);
	}
}
