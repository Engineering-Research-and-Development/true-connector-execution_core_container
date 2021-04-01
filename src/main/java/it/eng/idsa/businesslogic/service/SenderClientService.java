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
	  * @param targetURL
	  * @param httpHeaders
	  * @param requestBody
	  * @return
	  * @throws IOException
	  */
	 Response sendMultipartFormRequest(String targetURL, Headers httpHeaders, RequestBody requestBody) throws IOException;
	 
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
	  * Create multipart/mixed request from multipart message 
	  * @param multipartMessage
	  * @return
	  */
	 RequestBody createMultipartMixRequest(MultipartMessage multipartMessage, String payloadContentType);
	 
	 /**
	  * Create multipart form request with 2 fields - header and payload, with corresponding contents
	  * @param multipartMessage
	  * @param payloadContentType
	  * @return
	  */
	 RequestBody createMultipartFormRequest(MultipartMessage multipartMessage, String payloadContentType);
}
