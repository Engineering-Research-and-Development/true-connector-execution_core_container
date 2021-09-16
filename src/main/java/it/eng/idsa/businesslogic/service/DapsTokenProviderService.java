package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.DynamicAttributeToken;

public interface DapsTokenProviderService {
	
	String provideToken();
	
	/**
	 * Provide DynamiAttributeToken</br>
	 * Internally, calls provide token.
	 * @return
	 */
	DynamicAttributeToken getDynamicAtributeToken();
}
