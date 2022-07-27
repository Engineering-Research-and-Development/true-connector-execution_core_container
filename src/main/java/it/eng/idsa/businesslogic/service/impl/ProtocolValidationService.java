package it.eng.idsa.businesslogic.service.impl;

public interface ProtocolValidationService {

	/**
	 * <p>Validates the Forward-To address protocol if it matches with the one selected in the application.properties. If validation is false, then the protocol will be just added or overwritten.</p>
	 * <p>Examples:</p>
	 * 
	 * <p>validation - true:
	 * selected https -> forwardTo must be https://example.com -> method returns https://example.com</p>
	 * 
	 * <p>validation - false:
	 * selected https -> forwardTo can be example.com or whatever://example.com -> method returns https://example.com</p>
	 * 
	 * @param forwardTo the Forward-To address that is to be checked
	 * @return the correct Forward-To address
	 */
	public String validateProtocol(String forwardTo);

}