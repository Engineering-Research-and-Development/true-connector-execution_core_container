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
	
	String sendData(String endpoint, String data);
	String sendDataAsJson(String endpoint, String data, String contentType);
	void deleteRequest(String endpoint);
	String getRequest(String url);
}