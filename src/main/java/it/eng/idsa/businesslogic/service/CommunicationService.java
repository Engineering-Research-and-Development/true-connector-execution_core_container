package it.eng.idsa.businesslogic.service;

import org.springframework.http.HttpEntity;

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
	public String sendData(String endpoint, org.apache.http.HttpEntity entity);
	@Deprecated
	public String sendData(String endpoint, HttpEntity<?> entity);
	public String sendData(String endpoint, String data);
	
}