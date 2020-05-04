package it.eng.idsa.businesslogic.service.impl;


import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
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

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.TokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.MultipartMessageTransformerService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;


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
	MultipartMessageTransformerService multipartMessageTransformerService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Override
	public String getHeaderContentString(String body) {
		MultipartMessage deserializedMultipartMessage = multipartMessageTransformerService.parseMultipartMessage(body);
		return deserializedMultipartMessage.getHeaderContentString();
	}

	@Override
	public String getPayloadContent(String body) {
		MultipartMessage deserializedMultipartMessage = multipartMessageTransformerService.parseMultipartMessage(body);
		return deserializedMultipartMessage.getPayloadContent();
	}
	
	@Override
	public Message getMessage(String header) {
		Message message = null;
		try {
			message = new Serializer().deserialize(header, Message.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;
	} 
	
	@Override
	public String addToken(Message message, String token) {
		String output = null;
		try {
			String msgSerialized = new Serializer().serializePlainJson(message);
			Token tokenJsonValue = new TokenBuilder()
					._tokenFormat_(TokenFormat.JWT)
					._tokenValue_(token).build();
			String tokenValueSerialized=new Serializer().serializePlainJson(tokenJsonValue);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			JSONObject jsonObjectToken = (JSONObject) parser.parse(tokenValueSerialized);
			jsonObject.put("authorizationToken",jsonObjectToken);
			output=new Serializer().serializePlainJson(jsonObject);
		} catch (JsonProcessingException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	@Override
	public String removeToken(Message message) {
		String output = null;
		try {
			String msgSerialized = new Serializer().serializePlainJson(message);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			jsonObject.remove("authorizationToken");
			output=new Serializer().serializePlainJson(jsonObject);
		} catch (JsonProcessingException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	@Override
	public Message getMessage(Object header) {
		Message message = null;
		try {
			message = new Serializer().deserialize(String.valueOf(header), Message.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;

	}
	
	@Override
	public HttpEntity createMultipartMessage(String header, String payload, String frowardTo) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.addTextBody("header", header);
		if(payload != null) {
			multipartEntityBuilder.addTextBody("payload", payload);
		}
		if(frowardTo!=null) {
			multipartEntityBuilder.addTextBody("forwardTo", frowardTo);
		}

		multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.STRICT);
		try {
			FormBodyPart bodyHeaderPart;

			bodyHeaderPart = new FormBodyPart("header", new StringBody(header, ContentType.DEFAULT_TEXT)) {
				@Override
				protected void generateContentType(ContentBody body) {
				}
				@Override
				protected void generateTransferEncoding(ContentBody body){
				}
			};
			bodyHeaderPart.addField("Content-Lenght", ""+header.length());

			FormBodyPart bodyPayloadPart=null;
			if(payload != null) {
				bodyPayloadPart=new FormBodyPart("payload", new StringBody(payload, ContentType.DEFAULT_TEXT)) {
					@Override
					protected void generateContentType(ContentBody body) {
					}
					@Override
					protected void generateTransferEncoding(ContentBody body){
					}
				};
				bodyPayloadPart.addField("Content-Lenght", ""+payload.length());
			}

			FormBodyPart headerForwardTo=null;
			if(frowardTo!=null) {
				headerForwardTo=new FormBodyPart("forwardTo", new StringBody(frowardTo, ContentType.DEFAULT_TEXT)) {
					@Override
					protected void generateContentType(ContentBody body) {
					}
					@Override
					protected void generateTransferEncoding(ContentBody body){
					}
				};
				headerForwardTo.addField("Content-Lenght", ""+frowardTo.length());
			}

			if(frowardTo!=null) {
				multipartEntityBuilder.addPart(headerForwardTo);
			}
			multipartEntityBuilder.addPart(bodyHeaderPart);
			if(payload != null) {
				multipartEntityBuilder.addPart(bodyPayloadPart);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return multipartEntityBuilder.build();
	}
	
	@Override
	public String getToken(Message message) throws JsonProcessingException {
		String token = null;
		try {
			String msgSerialized = new Serializer().serializePlainJson(message);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			jsonObject=(JSONObject) jsonObject.get("authorizationToken");
			if(jsonObject == null) {
				logger.error("Token is not set: authorizationToken is not set in the part of the header in the multipart message");
				rejectionMessageService.sendRejectionMessage(
						RejectionMessageType.REJECTION_TOKEN, 
						message);
			} else {
				token= (String) jsonObject.get("tokenValue");
				if(token == null) {
					logger.error("Token is not set: tokenValue is not set in the part of the header in the multipart message");
					rejectionMessageService.sendRejectionMessage(
							RejectionMessageType.REJECTION_TOKEN, 
							message);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return token;
	}

}
