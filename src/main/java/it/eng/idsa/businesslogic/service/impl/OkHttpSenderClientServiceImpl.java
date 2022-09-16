package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.service.SenderClientService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class OkHttpSenderClientServiceImpl implements SenderClientService {

	private static final String MEDIA_TYPE_HEADER_JSON_LD = "application/ld+json";
	
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
		RequestBody body = null;
		if(StringUtils.isNotBlank(payload)) {
			body = RequestBody.create(
					payload,
					MediaType.parse(payloadContentType != null ? payloadContentType : javax.ws.rs.core.MediaType.TEXT_PLAIN));
		} else {
			body = RequestBody.create(new byte[]{}, null);
		}
		
		Request.Builder requestBuilder = new Request.Builder()
				  .headers(httpHeaders)
			      .url(targetURL)
			      .post(body);
		Request request = requestBuilder.build();
		return client.newCall(request).execute();
	}

	@Override
	public RequestBody createMultipartMixRequest(MultipartMessage message, String payloadContentType) {
		Part headerPart = Part.create(
				Headers.of(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"header\""),
				RequestBody.create(message.getHeaderContentString(), MediaType.parse(MEDIA_TYPE_HEADER_JSON_LD)));
		
		Part payloadPart = null;
		if(message.getPayloadContent() != null) {
			payloadPart = Part.create(Headers.of(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"payload\""),
			          RequestBody.create(message.getPayloadContent(), MediaType.parse(payloadContentType)));
		}
		
		MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
			      .setType(MultipartBody.MIXED)
			      .addPart(headerPart);
		if(payloadPart != null) {
			multipartBodyBuilder.addPart(payloadPart);
		}
		return multipartBodyBuilder.build();
	}

	@Override
	public RequestBody createMultipartFormRequest(MultipartMessage message, String payloadContentType) {
		Part headerPart = Part.create(
				Headers.of(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"header\""),
				RequestBody.create(message.getHeaderContentString(), MediaType.parse(MEDIA_TYPE_HEADER_JSON_LD)));
		
		Part payloadPart = null;
		if(message.getPayloadContent() != null && message.getPayloadHeader().get(HttpHeaders.CONTENT_DISPOSITION) != null) {
			payloadPart = Part.create(Headers.of(HttpHeaders.CONTENT_DISPOSITION, message.getPayloadHeader().get(HttpHeaders.CONTENT_DISPOSITION)),
			          RequestBody.create(message.getPayloadContent(), MediaType.parse(payloadContentType)));
		} else if(message.getPayloadContent() != null){
			payloadPart = Part.create(Headers.of(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"payload\""),
			          RequestBody.create(message.getPayloadContent(), MediaType.parse(payloadContentType)));
		}
		
		MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
			      .setType(MultipartBody.FORM)
			      .addPart(headerPart);
		if(payloadPart != null) {
			multipartBodyBuilder.addPart(payloadPart);
		}
		return multipartBodyBuilder.build();
	}

	@Override
	public Response sendMultipartMixRequestPayload(String targetURL, Headers httpHeaders, String payload) throws IOException {
		RequestBody body = null;
		body = RequestBody.create(
			payload,
			null);
	
		Request.Builder requestBuilder = new Request.Builder()
				  .headers(httpHeaders)
			      .url(targetURL)
			      .post(body);
		Request request = requestBuilder.build();
		return client.newCall(request).execute();
	}

}
