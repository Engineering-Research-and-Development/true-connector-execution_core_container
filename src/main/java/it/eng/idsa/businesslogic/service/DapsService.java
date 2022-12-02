package it.eng.idsa.businesslogic.service;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing DAPS.
 */
public interface DapsService {

	String getJwtToken();
	
	boolean validateToken(String tokenValue);
	
	boolean isDapsAvailable(String dapsHealthCheckEndpoint);
	
	String getConnectorUUID();
	
}
