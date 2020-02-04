package it.eng.idsa.businesslogic.service;

import org.apache.http.HttpEntity;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing DAPS.
 */
public interface CommunicationService {
	
	@Deprecated
	public String sendData(String endpoint, HttpEntity data);
	public String sendData(String endpoint, String data);
	
}