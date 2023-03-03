package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.DynamicAttributeToken;

public interface DapsTokenProviderService {
	
	String provideToken();
	
	/**
	 * Provide DynamiAttributeToken\n
	 * Internally, calls provide token.
	 * @return DynamicAttributeToken
	 */
	DynamicAttributeToken getDynamicAtributeToken();

	/**
	 * Get Connector UUID
	 * @return Connector UUID
	 */
	String getConnectorUUID();
}
