package it.eng.idsa.businesslogic.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.eng.idsa.multipart.domain.MultipartMessage;

public interface SendDataToBusinessLogicService {

	CloseableHttpResponse sendMessageBinary(String address, MultipartMessage message, Map<String, Object> httpHeaders,
			boolean eccCommunication)
			throws UnsupportedEncodingException, JsonProcessingException;

	CloseableHttpResponse sendMessageHttpHeader(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts, boolean eccCommunication) throws IOException;
	
	CloseableHttpResponse sendMessageFormData(String address, MultipartMessage message,
			Map<String, Object> headerParts, boolean eccCommunication) throws UnsupportedEncodingException;

}
