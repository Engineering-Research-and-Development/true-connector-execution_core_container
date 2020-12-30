package it.eng.idsa.businesslogic.service;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import it.eng.idsa.multipart.domain.MultipartMessage;

public interface HttpHeaderService {
	
	String getHeaderMessagePartFromHttpHeadersWithoutToken(Map<String, Object> headers) throws JsonProcessingException;

	Map<String, Object> prepareMessageForSendingAsHttpHeadersWithToken(String header) throws JsonParseException, JsonMappingException, IOException;

	String getHeaderMessagePartFromHttpHeadersWithToken(Map<String, Object> headers) throws JsonProcessingException;

	Map<String, Object> prepareMessageForSendingAsHttpHeadersWithoutToken(String header) throws JsonParseException, JsonMappingException, IOException;
	
	Map<String, Object> prepareMessageForSendingAsHttpHeaders(MultipartMessage multipartMessage) throws IOException;
	
	void removeTokenHeaders(Map<String, Object> headers);
	
	void removeMessageHeadersWithoutToken(Map<String, Object> headers);
	
	Map<String, Object> getHeaderMessagePartAsMap(Map<String, Object> headers);

	Map<String, Object> getHeaderContentHeaders(Map<String, Object> headersParts);
	
	Map<String, String> convertMapToStringString(Map<String, Object> map);
	
	Map<String, Object> transformJWTTokenToHeaders(String token)
			throws JsonMappingException, JsonProcessingException;
}
