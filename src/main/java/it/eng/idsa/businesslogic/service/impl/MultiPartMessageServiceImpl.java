package it.eng.idsa.businesslogic.service.impl;


import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.TokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.MultiPartMessageService;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */


/**
 * Service Implementation for managing MultiPartMessage.
 */
@Service
@Transactional
public class MultiPartMessageServiceImpl implements MultiPartMessageService {

	@Override
	public String getHeader(String body) {
		MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
		return deserializedMultipartMessage.getHeaderString();
	}

	@Override
	public String getPayload(String body) {
		MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
		return deserializedMultipartMessage.getPayload();
	}

	@Override
	public Message getMessage(String body) {
		MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
		return deserializedMultipartMessage.getHeader();
	}

	@Override
	public String addToken(Message message, String token) {
		try {
			String msgSerialized = new Serializer().serializePlainJson(message);
			Token tokenJsonValue = new TokenBuilder()
					._tokenFormat_(TokenFormat.JWT)
					._tokenValue_(token).build();
			String tokenValueSerialized=new Serializer().serializePlainJson(tokenJsonValue);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			JSONObject jsonObjectMark = (JSONObject) parser.parse(tokenValueSerialized);
			jsonObject.put("authorizationToken",jsonObjectMark);
			String output=new Serializer().serializePlainJson(jsonObject);
			return output;
		} catch (JsonProcessingException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Message getMessage(Object header) {
		Message message = null;
		try {
			message = SerializationHelper.getInstance().fromJsonLD(((String) header), Message.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;

	}
	
	
	
	public HttpEntity createMultipartMessage(String header, String payload/*, String boundary, String contentType*/) {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addTextBody("header", header, ContentType.APPLICATION_JSON);
        multipartEntityBuilder.addTextBody("payload", payload, ContentType.APPLICATION_JSON);
        // multipartEntityBuilder.setBoundary(boundary)
        HttpEntity multipart = multipartEntityBuilder.build();
        
		return multipart;
	}


}
