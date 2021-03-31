package it.eng.idsa.businesslogic.service;

import java.io.IOException;

import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

public interface SenderClientService {
	
	/**
	 * Sends multiaprt mix request. </br>
	 * For creating correct request body {@link #createMultipartMixRequest}
	 * @param targetURL
	 * @param httpHeaders
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	 Response sendMultipartMixRequest(String targetURL, Headers httpHeaders, RequestBody requestBody) throws IOException;
	
	 /**
	  * Sends multiaprt form request. </br>
	  * For creating correct form request body {@link #createMultipartFormRequest}
	  * @param requestBody
	  * @param targetURL
	  * @param httpHeaders
	  * @return
	  * @throws IOException
	  */
	 Response sendMultipartFormRequest(RequestBody requestBody, String targetURL, Headers httpHeaders) throws IOException;
	 
	 /**
	  * Create header request with request from multipartMessage.payload with correct payload content type
	  * @param targetURL
	  * @param httpHeaders
	  * @param payload
	  * @param payloadContentType
	  * @return
	  * @throws IOException
	  */
	 Response sendHttpHeaderRequest(String targetURL, Headers httpHeaders, String payload, String payloadContentType) throws IOException;
	 
	 
	 /*
	  * Utility methods for creating requests
	  */
	 
	 /**
	  * Create request with from multipart message 
	  * @param multipartMessage
	  * @param payloadContentType
	  * @return
	  */
	 RequestBody createMultipartMixRequest(String multipartMessage, String payloadContentType);
	 
	 /**
	  * Create multipart form request with 2 fields - header and payload, with corresponding contents
	  * @param message
	  * @param payloadContentType
	  * @return
	  */
	 RequestBody createMultipartFormRequest(MultipartMessage message, String payloadContentType);
}
