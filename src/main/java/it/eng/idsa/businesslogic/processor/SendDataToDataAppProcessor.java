package it.eng.idsa.businesslogic.processor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.domain.json.HeaderBodyJson;
import nl.tno.ids.common.communication.HttpClientGenerator;
import nl.tno.ids.common.config.keystore.KeystoreConfigType;
import nl.tno.ids.common.config.keystore.TruststoreConfig;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SendDataToDataAppProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(SendDataToDataAppProcessor.class);
	
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		
		// Get header and payload
		String header = filterHeader(multipartMessageParts.get("header").toString());
		String payload = multipartMessageParts.get("payload").toString();
		
		// Covert to ContentBody
		ContentBody cbHeader = convertToContentBody(header, ContentType.APPLICATION_JSON, "header");
		ContentBody cbPayload = convertToContentBody(payload, ContentType.DEFAULT_TEXT, "payload");
		
		// Send data to the endpoint F
		forwardMessage("http://"+configuration.getOpenDataAppReceiver()+"/incoming-data-app/router", cbHeader, cbPayload);
	}
	
	private int forwardMessage(String address, ContentBody cbHeader, ContentBody cbPayload) throws ClientProtocolException, IOException {
		logger.info("Forwarding Message");
		
		HttpPost httpPost = new HttpPost(address);
		
		HttpEntity reqEntity = MultipartEntityBuilder.create()
				.addPart("header", cbHeader)
				.addPart("payload", cbPayload)
				.build();
		
		httpPost.setEntity(reqEntity);
		
		CloseableHttpResponse response = getHttpClient().execute(httpPost);
		
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode > 300) {
			handleTheBadResponse(statusCode, response);
		}else {
			logger.info("OK result: {} {}", statusCode, response.getEntity().getContent());
		}
		response.close();
	    return statusCode;
	}
	
	private CloseableHttpClient getHttpClient() {
	  TruststoreConfig truststoreConfig = new TruststoreConfig();
	  truststoreConfig.setType(KeystoreConfigType.ACCEPT_ALL);
	  CloseableHttpClient httpClient = HttpClientGenerator.get(truststoreConfig);
	  logger.warn("Created Accept-All Http Client");
	  
	  return httpClient;
	}
	
	private String filterHeader(String header) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		HeaderBodyJson headerBodyJson = mapper.readValue(header, HeaderBodyJson.class);
		return mapper.writeValueAsString(headerBodyJson);
	}
	
	private ContentBody convertToContentBody(String value, ContentType contentType, String valueName) throws UnsupportedEncodingException {
		byte[] valueBiteArray = value.getBytes("utf-8");
		ContentBody cbValue = new ByteArrayBody(valueBiteArray, contentType, valueName);
		return cbValue;
	}
	
	private void handleTheBadResponse(int statusCode, CloseableHttpResponse response) throws UnsupportedOperationException, IOException {
		try (Scanner scanner = new Scanner(response.getEntity().getContent())) {
			String body = scanner.useDelimiter("\\A").next();
			response.getEntity().getContent();
	        logger.error("Bad result: {} {}", statusCode, body);
		}
	}
	
}
