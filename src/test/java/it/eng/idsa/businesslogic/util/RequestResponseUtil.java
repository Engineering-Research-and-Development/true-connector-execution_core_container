package it.eng.idsa.businesslogic.util;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RequestResponseUtil {
	private final String MEDIA_TYPE_HEADER_JSON_LD = "application/ld+json";
	private final String MOCK_ADDRESS = "http://mock.adress";

	public RequestBody getRequestBodyMultipart(MediaType type) {
		return new MultipartBody.Builder()
				.setType(type)
				.addPart(getPartHeader(MessagePart.HEADER), getRequestBodyPart(null))
				.addPart(getPartHeader(MessagePart.PAYLOAD), getRequestBodyPart(null))
				.build();
	}
	
	public static RequestBody createRequestBody(String payload) {
		return RequestBody.create(payload, MediaType.get("application/json; charset=utf-8"));
	}
	
	public static Request createRequest(String URL, RequestBody requestBody) {
		return  new Request.Builder()
				.url(URL)
				.header("accept", "application/json")
				.header("Content-Type", "application/json")
				.post(requestBody)
				.build();
	}
	
	public static ResponseBody createResponseBodyJsonUTF8(String responsePayload) {
		return ResponseBody.create(responsePayload, MediaType.get("application/json; charset=utf-8"));
	}
	
	public static Response createResponse(Request request, String message, ResponseBody responseBody, int statusCode ) {
		return new Response.Builder()
				.request(request)
				.protocol(Protocol.HTTP_1_1)
				.message(message)
				.body(responseBody)
				.code(statusCode)
				.build();
	} 
	
	
	private  RequestBody getRequestBodyPart(String partContent) {
		return RequestBody.create(partContent, MediaType.parse(MEDIA_TYPE_HEADER_JSON_LD));
	}

	private  Headers getPartHeader(String partName) {
		return Headers.of("Content-Disposition", "form-data; name=\"" + partName + "\"");
	}
	
	public Request getPostRequest(RequestBody requestBody, Headers headers) {
		return new Request.Builder()
				  .headers(headers)
			      .url(MOCK_ADDRESS)
			      .post(requestBody)
			      .build();
	}
	
}
