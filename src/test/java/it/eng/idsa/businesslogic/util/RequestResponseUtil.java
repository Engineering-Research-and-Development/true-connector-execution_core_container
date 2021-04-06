package it.eng.idsa.businesslogic.util;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RequestResponseUtil {
	private final String MEDIA_TYPE_HEADER_JSON_LD = "application/json+ld";
	private final String MOCK_ADDRESS = "http://mock.adress";

	public  RequestBody getRequestBodyMultipart(MediaType type) {
		return new MultipartBody.Builder()
				.setType(type)
				.addPart(getPartHeader("header"), getRequestBodyPart(null))
				.addPart(getPartHeader("payload"), getRequestBodyPart(null))
				.build();
	}
	
	private  RequestBody getRequestBodyPart(String partContent) {
		return RequestBody.create(MediaType.parse(MEDIA_TYPE_HEADER_JSON_LD), partContent);
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
