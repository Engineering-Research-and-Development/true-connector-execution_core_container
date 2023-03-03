package it.eng.idsa.businesslogic.service;

import java.util.Map;

import de.fraunhofer.iais.eis.Message;
import okhttp3.Headers;

public interface HttpHeaderService {
	
	/**
	 * Convert IDS Message to map of headers needed in http-header flow
	 * @param message IDS message
	 * @return Map of key value pairs of converted message
	 */
	Map<String, Object> messageToHeaders(Message message);
	
	/**
	 * Convert http-headers to IDS Message
	 * @param headers Map of key value pairs of "header" message
	 * @return IDS message
	 */
	Message headersToMessage(Map<String, Object> headers);
	
	Map<String, String> convertMapToStringString(Map<String, Object> map);
	
	/**
	 * Convert OkHttpHeader to map needed for converting headers to Message
	 * @param headers OkHttp headers
	 * @return Map of key value pairs of converted message
	 */
	Map<String, Object> okHttpHeadersToMap(Headers headers);
	
	Map<String, Object> getIDSHeaders(Map<String, Object> headers);
}
