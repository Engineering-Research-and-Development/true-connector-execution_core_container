package it.eng.idsa.businesslogic.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Response;

public interface SendDataToBusinessLogicService {

	Response sendMessageBinary(String address, MultipartMessage message, Map<String, Object> httpHeaders)
			throws UnsupportedEncodingException, JsonProcessingException;

	Response sendMessageHttpHeader(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts) throws IOException;
	
	Response sendMessageFormData(String address, MultipartMessage message,
			Map<String, Object> headerParts) throws UnsupportedEncodingException;

	void checkResponse(Message messageForRejection, Response response, String forwardTo);

}
