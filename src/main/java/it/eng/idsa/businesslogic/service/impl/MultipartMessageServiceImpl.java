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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

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
	private static final Logger logger = LogManager.getLogger(MultipartMessageServiceImpl.class);

	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Override
	public String getHeaderContentString(String body) {
		MultipartMessage deserializedMultipartMessage = MultipartMessageProcessor.parseMultipartMessage(body);
		return deserializedMultipartMessage.getHeaderContentString();
	}

	@Override
	public String getPayloadContent(String body) {
		MultipartMessage deserializedMultipartMessage = MultipartMessageProcessor.parseMultipartMessage(body);
		return deserializedMultipartMessage.getPayloadContent();
	}

	@Override
	public Message getMessage(String header) {
		Message message = null;
		try {
			message = new Serializer().deserialize(header, Message.class);
		} catch (IOException e) {
			logger.error(e);
		}
		return message;
	}

	@Override
	public String addToken(Message message, String token) {
		String output = null;
		try {
			String msgSerialized = serializeMessage(message);
			Token tokenJsonValue = new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_(token).build();
			String tokenValueSerialized = serializeMessage(tokenJsonValue);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			JSONObject jsonObjectToken = (JSONObject) parser.parse(tokenValueSerialized);
			jsonObject.put("ids:securityToken", jsonObjectToken);
			output = serializeMessage(jsonObject);
		} catch (ParseException | IOException e) {
			logger.error(e);
		}
		return output;
	}

	@Override
	public String removeToken(Message message) {
		String output = null;
		try {
			String msgSerialized = serializeMessage(message);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			jsonObject.remove("ids:securityToken");
			output = serializeMessage(jsonObject);
		} catch (IOException | ParseException e) {
			logger.error(e);
		}
		return output;
	}

	@Override
	public Message getMessage(Object header) {
		Message message = null;
		try {
			message = new Serializer().deserialize(String.valueOf(header), Message.class);
		} catch (IOException e) {
			logger.error(e);
		}
		return message;

	}

	@Override
	public HttpEntity createMultipartMessage(String header, String payload, String frowardTo, ContentType ctPayload) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.addTextBody("header", header);
		if (payload != null) {
			multipartEntityBuilder.addTextBody("payload", payload);
		}
		if (frowardTo != null) {
			multipartEntityBuilder.addTextBody("forwardTo", frowardTo);
		}

		multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.STRICT);
		try {
			FormBodyPart bodyHeaderPart;
			ContentBody headerBody = new StringBody(header, ContentType.APPLICATION_JSON);
			bodyHeaderPart = FormBodyPartBuilder.create("header", headerBody).build();
			bodyHeaderPart.addField("Content-Lenght", "" + header.length());

			FormBodyPart bodyPayloadPart = null;
			if (payload != null) {
				ContentBody payloadBody = new StringBody(payload, ctPayload);
				bodyPayloadPart = FormBodyPartBuilder.create("payload", payloadBody).build();
				bodyPayloadPart.addField("Content-Lenght", "" + payload.length());
			}

			FormBodyPart headerForwardTo = null;
			if (frowardTo != null) {
				ContentBody forwardToBody = new StringBody(frowardTo, ContentType.DEFAULT_TEXT);
				headerForwardTo = FormBodyPartBuilder.create("forwardTo", forwardToBody).build();
				headerForwardTo.addField("Content-Lenght", "" + frowardTo.length());
			}

			if (frowardTo != null) {
				multipartEntityBuilder.addPart(headerForwardTo);
			}
			multipartEntityBuilder.addPart(bodyHeaderPart);
			if (payload != null) {
				multipartEntityBuilder.addPart(bodyPayloadPart);
			}

		} catch (Exception e) {
			logger.error(e);
		}
		return multipartEntityBuilder.build();
	}

	@Override
	public String getToken(Message message) throws JsonProcessingException {
		String token = null;
		try {
			String msgSerialized = serializeMessage(message);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			jsonObject = (JSONObject) jsonObject.get("ids:securityToken");
			if (jsonObject == null) {
				logger.error(
						"Token is not set: securityToken is not set in the part of the header in the multipart message");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, message);
			} else {
				token = (String) jsonObject.get("ids:tokenValue");
				if (token == null) {
					logger.error(
							"Token is not set: tokenValue is not set in the part of the header in the multipart message");
					rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, message);
				}
			}
		} catch (IOException | ParseException e) {
			logger.error(e);
		}
		return token;
	}

	public static String serializeMessage(Object object) throws IOException {
//		String serializeToPlain = MultipartMessageProcessor.multipartMessagetoString((MultipartMessage) object);
		return MultipartMessageProcessor.serializeToPlainJson(object);
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
	public MultipartMessage removeTokenFromMultipart(MultipartMessage messageWithToken) {
		String messageWithoutToken = removeToken(messageWithToken.getHeaderContent());
		return new MultipartMessageBuilder()
				.withHttpHeader(messageWithToken.getHttpHeaders()).withHeaderHeader(messageWithToken.getHeaderHeader())
				.withHeaderContent(messageWithoutToken).withPayloadHeader(messageWithToken.getPayloadHeader())
				.withPayloadContent(messageWithToken.getPayloadContent()).withToken(messageWithToken.getToken())
				.build();
	}

	@Override
	public Message getMessageFromHeaderMap(Map<String, Object> headers) throws JsonProcessingException {
		String json = null;
		json = new ObjectMapper().writeValueAsString(headers);
		return getMessage(json);
	}
}
