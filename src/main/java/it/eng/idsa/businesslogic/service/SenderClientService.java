package it.eng.idsa.businesslogic.service;

import java.io.IOException;

import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

public interface SenderClientService {
	
	/**
	 * Sends multiaprt mix request.\n
	 * For creating correct request body {@link #createMultipartMixRequest}
	 * @param targetURL Destination URL
	 * @param httpHeaders Http headers
	 * @param requestBody request body
	 * @return Response
	 * @throws IOException exception if request failed
	 */
	 Response sendMultipartMixRequest(String targetURL, Headers httpHeaders, RequestBody requestBody) throws IOException;
	
	 /**
	 * Sends multiaprt mix request. \n
	 * For creating correct request body {@link #createMultipartMixRequest}
	 * @param targetURL Destination URL
	 * @param httpHeaders Http headers
	 * @param payloadContentType payload content type
	 * @return Response
	 * @throws IOException exception if request failed
	 */
	 Response sendMultipartMixRequestPayload(String targetURL, Headers httpHeaders, String payloadContentType) throws IOException;
		
	 /**
	  * Sends multiaprt form request. \n
	  * For creating correct form request body {@link #createMultipartFormRequest}
	  * @param targetURL Destination URL
	  * @param httpHeaders Http headers
	  * @param requestBody request body
	  * @return Response
	  * @throws IOException exception if request failed
	  */
	 Response sendMultipartFormRequest(String targetURL, Headers httpHeaders, RequestBody requestBody) throws IOException;
	 
	 /**
	  * Create header request with request from multipartMessage.payload with correct payload content type
	  * @param targetURL Destination URL
	  * @param httpHeaders Http headers
	  * @param payload Payload
	  * @param payloadContentType payload content type
	  * @return Response
	  * @throws IOException exception if request failed
	  */
	 Response sendHttpHeaderRequest(String targetURL, Headers httpHeaders, String payload, String payloadContentType) throws IOException;
	 
	 
	 /*
	  * Utility methods for creating requests
	  */
	 
	 /**
	  * Create multipart/mixed request from multipart message 
	  * @param multipartMessage ultipart message
	  * @param payloadContentType payload content type
	  * @return Request Body
	  */
	 RequestBody createMultipartMixRequest(MultipartMessage multipartMessage, String payloadContentType);
	 
	 /**
	  * Create multipart form request with 2 fields - header and payload, with corresponding contents
	  * @param multipartMessage ultipart message
	  * @param payloadContentType payload content type
	  * @return Request Body
	  */
	 RequestBody createMultipartFormRequest(MultipartMessage multipartMessage, String payloadContentType);
}
