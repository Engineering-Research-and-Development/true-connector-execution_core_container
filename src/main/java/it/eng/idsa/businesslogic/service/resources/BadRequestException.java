package it.eng.idsa.businesslogic.service.resources;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 6083985282191081481L;

	public BadRequestException(String message) {
		super(message);
	}
}
