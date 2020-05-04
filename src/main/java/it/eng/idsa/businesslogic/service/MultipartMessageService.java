package it.eng.idsa.businesslogic.service;

import org.apache.http.HttpEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public interface MultipartMessageService {

	public String getHeaderContentString(String body);
	public String getPayloadContent(String body);
	public Message getMessage(String header);
	public String addToken(Message message, String token);
	public String removeToken(Message message);
	public Message getMessage(Object header);
	public HttpEntity createMultipartMessage(String header, String payload, String frowardTo);
	public String getToken(Message message) throws JsonProcessingException;
	
}
