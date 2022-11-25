package it.eng.idsa.businesslogic.service;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing DAPS.
 */
public interface CommunicationService {
	
	public String sendData(String endpoint, String data);
	public String sendDataAsJson(String endpoint, String data, String contentType);
	public void deleteRequest(String endpoint);
	
}