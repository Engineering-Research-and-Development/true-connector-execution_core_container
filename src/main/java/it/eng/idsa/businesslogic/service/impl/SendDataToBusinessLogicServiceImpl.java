package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.service.SenderClientService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.MultipartMessageKey;
import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class SendDataToBusinessLogicServiceImpl implements SendDataToBusinessLogicService {

	private static final Logger logger = LoggerFactory.getLogger(SendDataToBusinessLogicServiceImpl.class);

	@Value("${camel.component.jetty.use-global-ssl-context-parameters}")
	private boolean isJettySSLEnabled;

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Value("${application.idscp2.isEnabled}")
	private boolean isEnabledIdscp2;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Autowired
	private HttpHeaderService headerService;

	@Autowired
	private HeaderCleaner headerCleaner;

	@Autowired
	private SenderClientService okHttpClient;

	@Override
	public Response sendMessageBinary(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts)
			throws UnsupportedEncodingException, JsonProcessingException {

		logger.info("Forwarding Message: Body: binary");
		
		Headers headers;
		if (isEnabledIdscp2) {
			Map<String, Object> headesParts = new HashMap<String, Object>();
			headesParts.put("idscp2", "idscp2");
			headers = fillHeaders(headesParts);
		} else {
			headers = fillHeaders(headerParts);
		}

		String payloadContentType = getPayloadContentType(headerParts);
		RequestBody requestBody = okHttpClient.createMultipartMixRequest(multipartMessage, payloadContentType);
//		String requestBody = MultipartMessageProcessor.multipartMessagetoString(multiMessage);
		try {
//			return okHttpClient.sendMultipartMixRequestPayload(address, headers, requestBody);
			return okHttpClient.sendMultipartMixRequest(address, headers, requestBody);

		} catch (IOException e) {
			logger.error("Error while calling Receiver", e);
		}
		return null;
	}

	@Override
	public Response sendMessageHttpHeader(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts) throws IOException {
		logger.info("Forwarding Message: http-header");

		headerParts.putAll(headerService.messageToHeaders(multipartMessage.getHeaderContent()));

		String ctPayload = getPayloadContentType(headerParts);
		Headers httpHeaders = fillHeaders(headerParts);

		Response response;
		try {
			response = okHttpClient.sendHttpHeaderRequest(address, httpHeaders, multipartMessage.getPayloadContent(),
					ctPayload);
		} catch (IOException e) {
			logger.error("Error while calling Receiver", e);
			return null;
		}
		return response;
	}

	@Override
	public Response sendMessageFormData(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts) throws UnsupportedEncodingException {

		logger.info("Forwarding Message: Body: form-data");

		String ctPayload = multipartMessage.getPayloadHeader().get("content-type") == null ? 
				MediaType.TEXT_PLAIN.toString() : multipartMessage.getPayloadHeader().get("content-type");
		Headers headers = fillHeaders(headerParts);
		RequestBody body = okHttpClient.createMultipartFormRequest(multipartMessage, ctPayload);

		Response response = null;
		try {
			response = okHttpClient.sendMultipartFormRequest(address, headers, body);
		} catch (IOException e) {
			logger.error("Error while sending form data request", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES);
		}
		return response;
	}

	private Headers fillHeaders(Map<String, Object> headerParts) {
		headerCleaner.removeTechnicalHeaders(headerParts);

		Map<String, String> mapAsString = new HashMap<>();
		// TODO consider removing idscp2-header from exchange once when it is consumed, if applicable - MapIDSCP2toMultipart
		headerParts.forEach((name, value) -> {
			if (!MultipartMessageKey.CONTENT_LENGTH.label.equals(name) && 
					!MultipartMessageKey.CONTENT_TYPE.label.equals(name) && !"idscp2-header".equals(name)) {
				if (value != null) {
					mapAsString.put(name, value.toString());
				}

			}
		});
		return Headers.of(mapAsString);
	}

	private String getPayloadContentType(Map<String, Object> headerParts) {
		String ctPayload = null;
		if (null != headerParts.get("Payload-Content-Type")) {
			ctPayload = (String) headerParts.get("Payload-Content-Type");
		} else {
			ctPayload = MediaType.TEXT_PLAIN.toString();
		}
		return ctPayload;
	}

	@Override
	public void checkResponse(Message message, Response response, String forwardTo) {
		if (response == null) {
			logger.info("...communication error");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES);
		} else {
			int statusCode = response.code();
			logger.debug("Response {}", response);
			logger.info("status code of the response message is: " + statusCode);
			if (statusCode >= 300) {
				if (statusCode == 404) {
					logger.info("...communication error - bad forwardTo URL " + forwardTo);
					rejectionMessageService
							.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES);
				}
				logger.info("data sent unsuccessfully to destination " + forwardTo);
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON);
			}
		}
	}
}