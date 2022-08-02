package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.TokenFormat;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.MultipartMessageKey;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Implementation for managing MultipartMessageServiceImpl.
 */
@Service
@Transactional
public class MultipartMessageServiceImpl implements MultipartMessageService {
	private static final Logger logger = LoggerFactory.getLogger(MultipartMessageServiceImpl.class);

	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Override
	public String addToken(Message message, String token) {
		String output = null;
		try {
			String msgSerialized = MultipartMessageProcessor.serializeToJsonLD(message);
			Token tokenJsonValue = new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_(token).build();
			String tokenValueSerialized = MultipartMessageProcessor.serializeToJsonLD(tokenJsonValue);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			JSONObject jsonObjectToken = (JSONObject) parser.parse(tokenValueSerialized);
			jsonObject.put("ids:securityToken", jsonObjectToken);
			output = MultipartMessageProcessor.serializeToJsonLD(jsonObject);
		} catch (ParseException | IOException e) {
			logger.error("Error while adding token to message", e);
		}
		return output;
	}

	// TODO remove? this method from this service, not related with multipartMessage
	// or once MultipartMethods are removed from this, rename it so that it reflects HttpEntity
	@Override
	public HttpEntity createMultipartMessage(String header, String payload, String frowardTo, ContentType ctPayload) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.addTextBody(MessagePart.HEADER, header);
		if (payload != null) {
			multipartEntityBuilder.addTextBody(MessagePart.PAYLOAD, payload);
		}
		if (frowardTo != null) {
			multipartEntityBuilder.addTextBody("forwardTo", frowardTo);
		}

		multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.STRICT);
		try {
			FormBodyPart bodyHeaderPart;
			ContentBody headerBody = new StringBody(header, ContentType.create("application/ld+json"));
			bodyHeaderPart = FormBodyPartBuilder.create(MessagePart.HEADER, headerBody).build();
			bodyHeaderPart.addField(MultipartMessageKey.CONTENT_LENGTH.label, String.valueOf(header.length()));

			FormBodyPart bodyPayloadPart = null;
			if (payload != null) {
				ContentBody payloadBody = new StringBody(payload, ctPayload);
				bodyPayloadPart = FormBodyPartBuilder.create(MessagePart.PAYLOAD, payloadBody).build();
				bodyPayloadPart.addField(MultipartMessageKey.CONTENT_LENGTH.label, String.valueOf(payload.length()));
			}

			FormBodyPart headerForwardTo = null;
			if (frowardTo != null) {
				ContentBody forwardToBody = new StringBody(frowardTo, ContentType.DEFAULT_TEXT);
				headerForwardTo = FormBodyPartBuilder.create("forwardTo", forwardToBody).build();
				headerForwardTo.addField(MultipartMessageKey.CONTENT_LENGTH.label, String.valueOf(frowardTo.length()));
			}

			if (frowardTo != null) {
				multipartEntityBuilder.addPart(headerForwardTo);
			}
			multipartEntityBuilder.addPart(bodyHeaderPart);
			if (payload != null) {
				multipartEntityBuilder.addPart(bodyPayloadPart);
			}

		} catch (Exception e) {
			logger.error("Error while creating multipart entity", e);
		}
		return multipartEntityBuilder.build();
	}

	@Override
	public String getToken(Message message) throws JsonProcessingException {
		String token = null;
		try {
			String msgSerialized = MultipartMessageProcessor.serializeToJsonLD(message);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			jsonObject = (JSONObject) jsonObject.get("ids:securityToken");
			if (jsonObject == null) {
				logger.error(
						"Token is not set: securityToken is not set in the part of the header in the multipart message");
				rejectionMessageService.sendRejectionMessage(message, RejectionReason.NOT_AUTHENTICATED);
			} else {
				token = (String) jsonObject.get("ids:tokenValue");
				if (token == null) {
					logger.error(
							"Token is not set: tokenValue is not set in the part of the header in the multipart message");
					rejectionMessageService.sendRejectionMessage(message, RejectionReason.NOT_AUTHENTICATED);
				}
			}
		} catch (IOException | ParseException e) {
			logger.error("Error while getting token from message", e);
		}
		return token;
	}

	@Override
	public MultipartMessage addTokenToMultipartMessage(MultipartMessage messageWithoutToken) {
		String messageWithToken = addToken(messageWithoutToken.getHeaderContent(), messageWithoutToken.getToken());
		return new MultipartMessageBuilder().withHttpHeader(messageWithoutToken.getHttpHeaders())
				.withHeaderHeader(messageWithoutToken.getHeaderHeader()).withHeaderContent(messageWithToken)
				.withPayloadHeader(messageWithoutToken.getPayloadHeader())
				.withPayloadContent(messageWithoutToken.getPayloadContent()).withToken(messageWithoutToken.getToken())
				.build();
	}

	@Override
	public Message getMessageFromHeaderMap(Map<String, Object> headers) throws JsonProcessingException {
		String json = null;
		json = new ObjectMapper().writeValueAsString(headers);
		return MultipartMessageProcessor.getMessage(json);
	}

}
