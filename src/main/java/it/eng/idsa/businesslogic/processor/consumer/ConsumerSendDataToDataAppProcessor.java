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
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.domain.json.HeaderBodyForOpenApiObject;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.communication.HttpClientGenerator;
import nl.tno.ids.common.config.keystore.AcceptAllTruststoreConfig;
import nl.tno.ids.common.config.keystore.TruststoreConfig;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.multipart.MultiPartMessage.Builder;
import nl.tno.ids.common.serialization.SerializationHelper;

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
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);

		// Get header, payload and message
		String header = filterHeader(multipartMessageParts.get("header").toString());
		String payload = multipartMessageParts.get("payload").toString();
		Message message = multiPartMessageServiceImpl.getMessage(multipartMessageParts.get("header"));

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
		default:
			Message rejectionCommunicationLocalIssues = multiPartMessageServiceImpl.createRejectionMessageLocalIssues(message);
			Builder builder = new MultiPartMessage.Builder();
			builder.setHeader(rejectionCommunicationLocalIssues);
			MultiPartMessage builtMessage = builder.build();
			String stringMessage = MultiPart.toString(builtMessage, false);
			exchange.getOut().setBody(stringMessage);
			exchange.getOut().setHeader("Content-Type", builtMessage.getHttpHeaders().getOrDefault("Content-Type", "multipart/mixed"));
			logger.error("Applicaton property: application.openDataAppReceiverRouter is not properly set");
			break;
		}

		// Handle response
		handleResponse(exchange, message, response);

		response.close();	

	}

	private CloseableHttpResponse forwardMessageBinary(String address, String header, String payload) throws ClientProtocolException, IOException {
		logger.info("Forwarding Message: Body: form-data");

		// Covert to ContentBody
		ContentBody cbHeader = convertToContentBody(header, ContentType.APPLICATION_JSON, "header");
		ContentBody cbPayload = convertToContentBody(payload, ContentType.DEFAULT_TEXT, "payload");

		// Set F address
		HttpPost httpPost = new HttpPost(address);

		HttpEntity reqEntity = MultipartEntityBuilder.create()
				.addPart("header", cbHeader)
				.addPart("payload", cbPayload)
				.build();

		httpPost.setEntity(reqEntity);

		CloseableHttpResponse response = getHttpClient().execute(httpPost);

		return response;
	}

	private CloseableHttpResponse forwardMessageFormData(String address, String header, String payload) throws ClientProtocolException, IOException {
		logger.info("Forwarding Message: Body: binary");

		// Set F address
		HttpPost httpPost = new HttpPost(address);

		HttpEntity reqEntity = multiPartMessageServiceImpl.createMultipartMessage(header, payload, null);
		httpPost.setEntity(reqEntity);

		CloseableHttpResponse response = getHttpClient().execute(httpPost);

		return response;
	}

	private CloseableHttpClient getHttpClient() {
		AcceptAllTruststoreConfig config=new AcceptAllTruststoreConfig();

		CloseableHttpClient httpClient = HttpClientGenerator.get(config);
		logger.warn("Created Accept-All Http Client");

		return httpClient;
	}

	private String filterHeader(String header) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		HeaderBodyForOpenApiObject headerBodyForOpenApiObject = mapper.readValue(header, HeaderBodyForOpenApiObject.class);
		return mapper.writeValueAsString(headerBodyForOpenApiObject);
	}

	private ContentBody convertToContentBody(String value, ContentType contentType, String valueName) throws UnsupportedEncodingException {
		byte[] valueBiteArray = value.getBytes("utf-8");
		ContentBody cbValue = new ByteArrayBody(valueBiteArray, contentType, valueName);
		return cbValue;
	}

	private void handleResponse(Exchange exchange, Message message, CloseableHttpResponse response) throws UnsupportedOperationException, IOException {
		// TODO: Check if response is multipart
		String responseString=new String(response.getEntity().getContent().readAllBytes());
		logger.info("content type response received from the DataAPP="+response.getFirstHeader("Content-Type"));
		logger.info("response received from the DataAPP="+responseString);
		
		int statusCode = response.getStatusLine().getStatusCode();
		logger.info("status code of the response message is: " + statusCode);
		if (statusCode >=300) { 
			Message rejectionCommunicationLocalIssues = multiPartMessageServiceImpl.createRejectionMessage(message);
			Builder builder = new MultiPartMessage.Builder();
			builder.setHeader(rejectionCommunicationLocalIssues); 
			MultiPartMessage builtMessage = builder.build(); 
			String stringMessage = MultiPart.toString(builtMessage, false);
			throw new ExceptionForProcessor(stringMessage);
		}else { 
			String	header = multiPartMessageServiceImpl.getHeader(responseString);
			String payload = multiPartMessageServiceImpl.getPayload(responseString);
			exchange.getOut().setHeader("header", header);
			exchange.getOut().setHeader("payload", payload);
		}
		 
	}

}
