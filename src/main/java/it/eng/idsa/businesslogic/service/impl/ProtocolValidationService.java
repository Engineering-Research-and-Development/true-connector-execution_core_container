package it.eng.idsa.businesslogic.service.impl;

import de.fraunhofer.iais.eis.Message;

public interface ProtocolValidationService {

	/**
	 * <p>Validates the Forward-To address protocol if it matches with the one selected in the application.properties. 
	 * If validation is false, then the protocol will be just added or overwritten.</p>
	 * <p>Examples:</p>
	 * 
	 * <p>validation - true:
	 * selected https -&gt; forwardTo must be https://example.com -&gt; method returns https://example.com</p>
	 * 
	 * <p>validation - false:
	 * selected https -&gt; forwardTo can be example.com or whatever://example.com -&gt; method returns https://example.com</p>
	 * 
	 * @param forwardTo the Forward-To address that is to be checked
	 * @param message request message needed in case of rejection
	 * @return the correct Forward-To address
	 */
	public String validateProtocol(String forwardTo, Message message);

}