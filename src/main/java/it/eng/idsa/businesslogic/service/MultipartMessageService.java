package it.eng.idsa.businesslogic.service;

import java.io.IOException;
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

	String getHeaderContentString(String body);
	String getPayloadContent(String body);
	Message getMessage(String header);
	String addToken(Message message, String token);
	String removeToken(Message message);
	Message getMessage(Object header) throws IOException;
	HttpEntity createMultipartMessage(String header, String payload, String frowardTo,ContentType ctPayload);
	String getToken(Message message) throws JsonProcessingException;
	Message getMessageFromHeaderMap(Map<String, Object> headers) throws JsonProcessingException;
	
	MultipartMessage addTokenToMultipartMessage(MultipartMessage messageWithoutToken);
	MultipartMessage removeTokenFromMultipart(MultipartMessage messageWithToken);
	Message removeTokenFromMessage(Message messageWithToken);
	
}
