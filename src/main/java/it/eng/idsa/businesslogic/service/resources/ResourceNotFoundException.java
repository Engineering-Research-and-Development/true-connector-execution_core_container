package it.eng.idsa.businesslogic.service.resources;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 6083985282191081481L;

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
