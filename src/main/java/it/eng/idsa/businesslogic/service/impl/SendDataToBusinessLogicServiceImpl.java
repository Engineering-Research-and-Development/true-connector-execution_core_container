package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
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

	@Value("${application.idscp2.isEnabled}")
	private boolean isEnabledIdscp2;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired
	private HttpHeaderService headerService;

	@Autowired
	private HeaderCleaner headerCleaner;

	@Autowired
	private SenderClientService okHttpClient;

	@Override
	public Response sendMessageBinary(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts, boolean eccCommunication)
			throws UnsupportedEncodingException, JsonProcessingException {

		logger.info("Forwarding Message: Body: binary");
		MultipartMessage multiMessage;
		if (!eccCommunication) {
			// sending to DataApp, remove token from message
			String header = multipartMessageService.removeToken(multipartMessage.getHeaderContent());
			multiMessage = new MultipartMessageBuilder().withHttpHeader(multipartMessage.getHttpHeaders())
					.withHeaderHeader(multipartMessage.getHeaderHeader()).withHeaderContent(header)
					.withPayloadHeader(multipartMessage.getPayloadHeader())
					.withPayloadContent(multipartMessage.getPayloadContent()).build();
		} else {
			multiMessage = multipartMessage;
		}
		
		Headers headers;
		if (isEnabledIdscp2) {
			Map<String, Object> headesParts = new HashMap<String, Object>();
			headesParts.put("idscp2", "idscp2");
			headers = fillHeaders(headesParts);
		} else {
			headers = fillHeaders(headerParts);
		}

		String payloadContentType;
		if (headerParts.get("Payload-Content-Type") != null) {
			payloadContentType = headerParts.get("Payload-Content-Type").toString();
		} else {
			payloadContentType = javax.ws.rs.core.MediaType.TEXT_PLAIN.toString();
		}
		RequestBody requestBody = okHttpClient.createMultipartMixRequest(multiMessage, payloadContentType);
		try {
			return okHttpClient.sendMultipartMixRequest(address, headers, requestBody);
		} catch (IOException e) {
			logger.error("Error while calling Receiver", e);
		}
		return null;
	}

	@Override
	public Response sendMessageHttpHeader(String address, MultipartMessage multipartMessage,
			Map<String, Object> headerParts, boolean eccCommunication) throws IOException {
		logger.info("Forwarding Message: http-header");

		if (!"http-header".equals(openDataAppReceiverRouter)) {
			// DataApp endpoint not http-header, must convert message to http headers
			headerParts.putAll(headerService.prepareMessageForSendingAsHttpHeaders(multipartMessage));
		}
		if (eccCommunication && isEnabledDapsInteraction) {
			headerParts.putAll(headerService.transformJWTTokenToHeaders(multipartMessage.getToken()));
		}
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
			Map<String, Object> headerParts, boolean eccCommunication) throws UnsupportedEncodingException {

		logger.info("Forwarding Message: Body: form-data");

		Message messageForException = multipartMessage.getHeaderContent();
		if (!eccCommunication) {
			// sending to DataApp, remove token from message
			multipartMessage = multipartMessageService.removeTokenFromMultipart(multipartMessage);
		}

		String ctPayload = getPayloadContentType(headerParts);
		Headers headers = fillHeaders(headerParts);
		RequestBody body = okHttpClient.createMultipartFormRequest(multipartMessage, ctPayload);

		Response response = null;
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

	private String getPayloadContentType(Map<String, Object> headerParts) {
		String ctPayload = null;
		if (null != headerParts.get("Payload-Content-Type")) {
			ctPayload = (String) headerParts.get("Payload-Content-Type");
		} else {
			ctPayload = javax.ws.rs.core.MediaType.TEXT_PLAIN.toString();
		}
		return ctPayload;
	}

	@Override
	public void checkResponse(Message message, Response response, String forwardTo) {
		if (response == null) {
			logger.info("...communication error");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		} else {
			int statusCode = response.code();
			logger.info("status code of the response message is: " + statusCode);
			if (statusCode >= 300) {
				if (statusCode == 404) {
					logger.info("...communication error - bad forwardTo URL " + forwardTo);
					rejectionMessageService
							.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, message);
				}
				logger.info("data sent unuccessfully to destination " + forwardTo);
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			}
		}
	}
}