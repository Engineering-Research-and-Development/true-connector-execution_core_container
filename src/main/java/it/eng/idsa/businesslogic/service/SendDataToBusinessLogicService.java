package it.eng.idsa.businesslogic.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Response;

public interface SendDataToBusinessLogicService {

	CloseableHttpResponse sendMessageBinary(String address, MultipartMessage message, Map<String, Object> httpHeaders,
			boolean eccCommunication)
			throws UnsupportedEncodingException, JsonProcessingException;

	Response sendMessageHttpHeader(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts, boolean eccCommunication) throws IOException;
	
	Response sendMessageFormData(String address, MultipartMessage message,
			Map<String, Object> headerParts, boolean eccCommunication) throws UnsupportedEncodingException;

}
