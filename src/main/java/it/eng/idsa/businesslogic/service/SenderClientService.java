package it.eng.idsa.businesslogic.service;

import java.io.IOException;

import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

public interface SenderClientService {
	
	 Response sendMultipartMixRequest(RequestBody requestBody, String targetURL, Headers httpHeaders) throws IOException;
	 Response sendMultipartFormRequest(RequestBody requestBody, String targetURL, Headers httpHeaders) throws IOException;
	 Response sendHttpHeaderRequest(MultipartMessage message, String targetURL) throws IOException;
	 
	 RequestBody createMultipartMixRequest(String multipartMessage, String payloadContentType);
	 RequestBody createMultipartFormRequest(MultipartMessage message, String payloadContentType);
}
