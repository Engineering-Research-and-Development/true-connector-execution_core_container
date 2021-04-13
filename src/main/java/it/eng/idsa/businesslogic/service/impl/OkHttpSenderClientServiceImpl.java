package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.service.SenderClientService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class OkHttpSenderClientServiceImpl implements SenderClientService {

	private static final String MEDIA_TYPE_HEADER_JSON_LD = "application/json+ld";
	
	@Autowired
	private OkHttpClient client;
	
	@Override
	public Response sendMultipartMixRequest(String targetURL, Headers httpHeaders, RequestBody requestBody) throws IOException {
		  Request request = new Request.Builder()
				  .headers(httpHeaders)
			      .url(targetURL)
			      .post(requestBody)
			      .build();
			 return client.newCall(request).execute();
	}

	@Override
	public Response sendMultipartFormRequest(String targetURL, Headers httpHeaders, RequestBody requestBody) throws IOException {
		Request request = new Request.Builder()
			  .headers(httpHeaders)
		      .url(targetURL)
		      .post(requestBody)
		      .build();
		 return client.newCall(request).execute();
	}

	@Override
	public Response sendHttpHeaderRequest(String targetURL, Headers httpHeaders, String payload, String payloadContentType) 
			throws IOException {
		RequestBody body = RequestBody.create(
			     MediaType.parse(payloadContentType != null ? payloadContentType : javax.ws.rs.core.MediaType.TEXT_PLAIN), 
			     payload);
		
		Request request = new Request.Builder()
				  .headers(httpHeaders)
			      .url(targetURL)
			      .post(body)
			      .build();
			 return client.newCall(request).execute();
	}

	@Override
	public RequestBody createMultipartMixRequest(MultipartMessage message, String payloadContentType) {
		return new MultipartBody.Builder()
			      .setType(MultipartBody.MIXED)
			      .addPart(
			          Headers.of("Content-Disposition", "form-data; name=\"header\""),
			          RequestBody.create(MediaType.parse(MEDIA_TYPE_HEADER_JSON_LD), message.getHeaderContentString()))
			      .addPart(
			          Headers.of("Content-Disposition", "form-data; name=\"payload\""),
			          RequestBody.create(MediaType.parse(payloadContentType), message.getPayloadContent()))
			      .build();
	}

	@Override
	public RequestBody createMultipartFormRequest(MultipartMessage message, String payloadContentType) {
		return new  MultipartBody.Builder()
			      .setType(MultipartBody.FORM)
			      .addPart(
			          Headers.of("Content-Disposition", "form-data; name=\"header\""),
			          RequestBody.create(MediaType.parse(MEDIA_TYPE_HEADER_JSON_LD), message.getHeaderContentString()))
			      .addPart(
			          Headers.of("Content-Disposition", "form-data; name=\"payload\""),
			          RequestBody.create(MediaType.parse(payloadContentType), message.getPayloadContent()))
			      .build();
	}
}
