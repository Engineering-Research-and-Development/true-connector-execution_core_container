package it.eng.idsa.businesslogic.processor.consumer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.communication.HttpClientGenerator;
import it.eng.idsa.businesslogic.util.config.keystore.AcceptAllTruststoreConfig;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerSendDataToDataAppProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ConsumerSendDataToDataAppProcessor.class);

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;
	
	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> headerParts = exchange.getIn().getHeaders();
        Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
        
		HeaderCleaner.removeTechnicalHeaders(headerParts);
 

        // Get header, payload and message
        String header = filterHeader(multipartMessageParts.get("header").toString());
        String payload = null;
        if(multipartMessageParts.containsKey("payload")) {
            payload = multipartMessageParts.get("payload").toString();
        }
		
		Message message = multipartMessageService.getMessage(multipartMessageParts.get("header"));

		// Send data to the endpoint F for the Open API Data App
		CloseableHttpResponse response = null;
		switch(openDataAppReceiverRouter) {
		case "mixed":
		{
			response =  forwardMessageBinary(configuration.getOpenDataAppReceiver(), header, payload);
			break;
		}
		case "form":
		{
			response =  forwardMessageFormData(configuration.getOpenDataAppReceiver(), header, payload);
			break;
		}
		default: {
			logger.error("Applicaton property: application.openDataAppReceiverRouter is not properly set");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, 
					message);
		}
		}

		// Handle response
		handleResponse(exchange, message, response, configuration.getOpenDataAppReceiver());

		if(response!=null) {
			response.close();
		}	

	}

	private CloseableHttpResponse forwardMessageBinary(String address, String header, String payload) throws ClientProtocolException, IOException {
		logger.info("Forwarding Message: Body: binary");

		// Covert to ContentBody
		ContentBody cbHeader = convertToContentBody(header, ContentType.APPLICATION_JSON, "header");
		ContentBody cbPayload = null;
		if(payload!=null) {
			cbPayload = convertToContentBody(payload, ContentType.DEFAULT_TEXT, "payload");
		}

		// Set F address
		HttpPost httpPost = new HttpPost(address);

		HttpEntity reqEntity = payload==null ?
			MultipartEntityBuilder.create()
				.addPart("header", cbHeader)
				.build()	
				:
			MultipartEntityBuilder.create()
				.addPart("header", cbHeader)
				.addPart("payload", cbPayload)
				.build();

		httpPost.setEntity(reqEntity);
		CloseableHttpResponse response;
		
		try {
			response = getHttpClient().execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return response;
	}

	private CloseableHttpResponse forwardMessageFormData(String address, String header, String payload) throws ClientProtocolException, IOException {
		logger.info("Forwarding Message: Body: form-data");

		// Set F address
		HttpPost httpPost = new HttpPost(address);

		HttpEntity reqEntity = multipartMessageService.createMultipartMessage(header, payload, null);
		httpPost.setEntity(reqEntity);

		CloseableHttpResponse response;
		
		try {
			response = getHttpClient().execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return response;
	}

	private CloseableHttpClient getHttpClient() {
		AcceptAllTruststoreConfig config = new AcceptAllTruststoreConfig();

		CloseableHttpClient httpClient = HttpClientGenerator.get(config);
		logger.warn("Created Accept-All Http Client");

		return httpClient;
	}

	private String filterHeader(String header) throws JsonMappingException, JsonProcessingException {
		Message message = multipartMessageService.getMessage(header);
		return multipartMessageService.removeToken(message);
	}

	private ContentBody convertToContentBody(String value, ContentType contentType, String valueName) throws UnsupportedEncodingException {
		byte[] valueBiteArray = value.getBytes("utf-8");
		ContentBody cbValue = new ByteArrayBody(valueBiteArray, contentType, valueName);
		return cbValue;
	}

	private void handleResponse(Exchange exchange, Message message, CloseableHttpResponse response, String openApiDataAppAddress) throws UnsupportedOperationException, IOException {
		if (response==null) {
			logger.info("...communication error with: " + openApiDataAppAddress);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, 
					message);
		} else {
			String responseString=new String(response.getEntity().getContent().readAllBytes());
			logger.info("content type response received from the DataAPP="+response.getFirstHeader("Content-Type"));
			logger.info("response received from the DataAPP="+responseString);

			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("status code of the response message is: " + statusCode);
			if (statusCode >=300) { 
				logger.info("data sent to destination: "+openApiDataAppAddress);
				rejectionMessageService.sendRejectionMessage(
						RejectionMessageType.REJECTION_MESSAGE_COMMON, 
						message);
			}else { 
				logger.info("data sent to destination: "+openApiDataAppAddress);
				logger.info("Successful response: "+ responseString);
				String	header = multipartMessageService.getHeaderContentString(responseString);
				String payload = multipartMessageService.getPayloadContent(responseString);
				HeaderCleaner.removeTechnicalHeaders(exchange.getIn().getHeaders());
				exchange.getOut().setHeaders(exchange.getIn().getHeaders());
				exchange.getOut().setHeader("header", header);
				if(payload!=null) {
					exchange.getOut().setHeader("payload", payload);
				}
			}
		}
	}

}
