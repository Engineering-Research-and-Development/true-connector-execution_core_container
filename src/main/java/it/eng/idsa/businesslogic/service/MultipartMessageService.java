package it.eng.idsa.businesslogic.service;

import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public interface MultipartMessageService {

	String addToken(Message message, String token);
	HttpEntity createMultipartMessage(String header, String payload, String frowardTo,ContentType ctPayload);
	String getToken(Message message) throws JsonProcessingException;
	Message getMessageFromHeaderMap(Map<String, Object> headers) throws JsonProcessingException;
	
	MultipartMessage addTokenToMultipartMessage(MultipartMessage messageWithoutToken);
}
