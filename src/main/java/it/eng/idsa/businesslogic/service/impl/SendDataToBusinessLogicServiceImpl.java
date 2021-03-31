package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.service.SenderClientService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.communication.HttpClientGenerator;
import it.eng.idsa.businesslogic.util.communication.HttpClientProvider;
import it.eng.idsa.businesslogic.util.config.keystore.AcceptAllTruststoreConfig;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class SendDataToBusinessLogicServiceImpl implements SendDataToBusinessLogicService {

	private static final Logger logger = LogManager.getLogger(SendDataToBusinessLogicServiceImpl.class);

	@Value("${camel.component.jetty.use-global-ssl-context-parameters}")
	private boolean isJettySSLEnabled;

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private HttpHeaderService headerService;

	@Autowired
	private HeaderCleaner headerCleaner;
	
	@Autowired
	private HttpClientProvider httpProvider;
	
	@Autowired
	private SenderClientService okHttpClient;

	@Override
	public CloseableHttpResponse sendMessageBinary(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts, boolean eccCommunication) throws UnsupportedEncodingException, JsonProcessingException {

		String header = null;
		String payload = multipartMessage.getPayloadContent();
		Message messageForExcepiton = multipartMessage.getHeaderContent();

		logger.info("Forwarding Message: Body: binary");
		
		if(!eccCommunication) {
			// sending to DataApp, remove token from message
			header = multipartMessageService.removeToken(multipartMessage.getHeaderContent());
		} else {
			header = multipartMessage.getHeaderContentString();
		}
		ContentType ctPayload;
		if (headerParts.get("Payload-Content-Type") != null) {
			ctPayload = ContentType
					.parse(headerParts.get("Payload-Content-Type").toString());
		} else {
			ctPayload = ContentType.TEXT_PLAIN;
		}
		// Covert to ContentBody
		ContentType ctHeader;
		if(multipartMessage.getHeaderHeader().containsKey("Content-Type")) {
			ctHeader = ContentType.parse(multipartMessage.getHeaderHeader().get("Content-Type"));
		} else {
			ctHeader = ContentType.APPLICATION_JSON;
		}
		ContentBody cbHeader = this.convertToContentBody(header, ctHeader, "header");
		ContentBody cbPayload = null;

		if (payload != null) {
			cbPayload = convertToContentBody(payload, ctPayload, "payload");
		}

		// Set F address
		HttpPost httpPost = new HttpPost(address);

		addHeadersToHttpPost(headerParts, httpPost);

//		httpPost.addHeader("headerHeaders", headerHeadersAsString);
//		httpPost.addHeader("payloadHeaders", payloadHeadersAsString);

		HttpEntity reqEntity = payload == null ? MultipartEntityBuilder.create().addPart("header", cbHeader).build()
				: MultipartEntityBuilder.create().addPart("header", cbHeader).addPart("payload", cbPayload).build();

		httpPost.setEntity(reqEntity);

		CloseableHttpResponse response = null;
		try {
//			response = getHttpClient().execute(httpPost);
			response = httpProvider.get().execute(httpPost);
		} catch (IOException e) {
			logger.error(e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					messageForExcepiton);
		}

		return response;
	}

	@Override
	public CloseableHttpResponse sendMessageHttpHeader(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts, boolean eccCommunication) throws IOException {
		logger.info("Forwarding Message: http-header");

		if(!"http-header".equals(openDataAppReceiverRouter)) {
			// DataApp endpoint not http-header, must convert message to http headers
			headerParts.putAll(headerService.prepareMessageForSendingAsHttpHeaders(multipartMessage));
		}
		
		if (eccCommunication && isEnabledDapsInteraction) {
			headerParts.putAll(headerService.transformJWTTokenToHeaders(multipartMessage.getToken()));
		}
		// Set F address
		HttpPost httpPost = new HttpPost(address);

		ContentType ctPayload;
		if (headerParts.get("Payload-Content-Type") != null) {
			ctPayload = ContentType
					.parse(headerParts.get("Payload-Content-Type").toString());
		} else {
			ctPayload = ContentType.TEXT_PLAIN;
		}
		if (multipartMessage.getPayloadContent() != null) {
			StringEntity payloadEntity = new StringEntity(multipartMessage.getPayloadContent(),ctPayload);
			httpPost.setEntity(payloadEntity);
		}
		
		headerParts.putAll(multipartMessage.getHttpHeaders());
		addHeadersToHttpPost(headerParts, httpPost);
		

		CloseableHttpResponse response;

		try {
			response = getHttpClient().execute(httpPost);
		} catch (IOException e) {
			logger.error("Error while calling Receiver", e);
			return null;
		}
		return response;
	}

	private ContentBody convertToContentBody(String value, ContentType contentType, String valueName)
			throws UnsupportedEncodingException {
		byte[] valueBiteArray = value.getBytes("utf-8");
		ContentBody cbValue = new ByteArrayBody(valueBiteArray, contentType, valueName);
		return cbValue;
	}

	private CloseableHttpClient getHttpClient() {
		AcceptAllTruststoreConfig config = new AcceptAllTruststoreConfig();

		CloseableHttpClient httpClient = HttpClientGenerator.get(config, isJettySSLEnabled);
		logger.warn("Created Accept-All Http Client");

		return httpClient;
	}

	private void addHeadersToHttpPost(Map<String, Object> headesParts, HttpPost httpPost) {
		headerCleaner.removeTechnicalHeaders(headesParts);

		headesParts.forEach((name, value) -> {
			if (!"Content-Length".equals(name) && !"Content-Type".equals(name)) {
				if (value != null) {
					httpPost.setHeader(name, value.toString());
				} else {
					httpPost.setHeader(name, null);
				}

			}
		});
	}


	@Override
	public Response sendMessageFormData(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts, boolean eccCommunication) throws UnsupportedEncodingException {
		
		logger.info("Forwarding Message: Body: form-data");
		
		Message messageForException = multipartMessage.getHeaderContent();
		
		if(!eccCommunication) {
			// sending to DataApp, remove token from message
			multipartMessage = multipartMessageService.removeTokenFromMultipart(multipartMessage);
		}
		
		String ctPayload;
		if (null != headerParts.get("Payload-Content-Type")) {
			ctPayload = (String) headerParts.get("Payload-Content-Type");
		} else {
			ctPayload = ContentType.TEXT_PLAIN.getMimeType();
		}
		
		Headers headers = fillHeaders(headerParts);

		RequestBody body = okHttpClient.createMultipartFormRequest(multipartMessage, ctPayload);

		Response response=null;		
		try {
			response = okHttpClient.sendMultipartFormRequest(address, headers, body);
		} catch (IOException e) {
			logger.error(e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					messageForException);
		}
		return response;
	}

	private Headers fillHeaders(Map<String, Object> headerParts) {
		headerCleaner.removeTechnicalHeaders(headerParts);
		
		Map<String, String> mapAsString = new HashMap<>();
		
		headerParts.forEach((name, value) -> {
			if (!"Content-Length".equals(name) && !"Content-Type".equals(name)) {
				if (value != null) {
					mapAsString.put(name, value.toString());
				}

			}
		});
		return Headers.of(mapAsString);
	}

}
