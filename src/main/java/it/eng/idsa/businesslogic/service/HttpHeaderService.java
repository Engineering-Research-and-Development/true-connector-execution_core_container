package it.eng.idsa.businesslogic.service;

import java.util.Map;

import de.fraunhofer.iais.eis.Message;

public interface HttpHeaderService {
	
	Map<String, Object> messageToHeaders(Message message);
	Message headersToMessage(Map<String, Object> headers);
	
	Map<String, String> convertMapToStringString(Map<String, Object> map);
	/*
	String getHeaderMessagePartFromHttpHeadersWithoutToken(Map<String, Object> headers) throws JsonProcessingException;

	Map<String, Object> prepareMessageForSendingAsHttpHeadersWithToken(String header) throws JsonParseException, JsonMappingException, IOException;

	String getHeaderMessagePartFromHttpHeadersWithToken(Map<String, Object> headers) throws JsonProcessingException;

	Map<String, Object> prepareMessageForSendingAsHttpHeadersWithoutToken(String header) throws JsonParseException, JsonMappingException, IOException;
	
	Map<String, Object> prepareMessageForSendingAsHttpHeaders(MultipartMessage multipartMessage) throws IOException;
	
	void removeTokenHeaders(Map<String, Object> headers);
	
	void removeMessageHeadersWithoutToken(Map<String, Object> headers);
	
	Map<String, Object> getHeaderMessagePartAsMap(Map<String, Object> headers);

	Map<String, Object> getHeaderContentHeaders(Map<String, Object> headersParts);
	
	
	Map<String, Object> transformJWTTokenToHeaders(String token)
			throws JsonMappingException, JsonProcessingException;
*/
}
