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
	/**
	 * Created Http Entity from header and payload
	 * @param header IDS message as string
	 * @param payload payload part
	 * @param frowardTo Forward-To URL
	 * @param ctPayload payload content type
	 * @return HttpEntity
	 */
	HttpEntity createMultipartMessage(String header, String payload, String frowardTo, ContentType ctPayload);
	
	/**
	 * Get token from IDS message
	 * @param message IDS Message containing token
	 * @return Token value
	 * @throws JsonProcessingException exception
	 */
	String getToken(Message message) throws JsonProcessingException;
	
	/**
	 * 
	 * @param headers Headers representation of IDS message
	 * @return IDS Message
	 * @throws JsonProcessingException exception
	 */
	Message getMessageFromHeaderMap(Map<String, Object> headers) throws JsonProcessingException;
	
	MultipartMessage addTokenToMultipartMessage(MultipartMessage messageWithoutToken);
}
