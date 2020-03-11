package it.eng.idsa.businesslogic.processor.producer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fhg.aisec.ids.comm.client.IdscpClient;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketConfiguration;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.FileStreamingBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.IdscpClientBean;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import nl.tno.ids.common.communication.HttpClientGenerator;
import nl.tno.ids.common.config.keystore.AcceptAllTruststoreConfig;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerSendDataToBusinessLogicProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ProducerSendDataToBusinessLogicProcessor.class);
	
	@Value("${application.isEnabledIdscp}")
	private boolean isEnabledIdscp;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private RejectionMessageServiceImpl rejectionMessageServiceImpl;
	
	@Autowired
	private WebSocketConfiguration webSocketConfiguration;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headesParts = exchange.getIn().getHeaders();
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		
		String messageWithToken = null;
		
		if(Boolean.parseBoolean(headesParts.get("Is-Enabled-Daps-Interaction").toString())) {
			messageWithToken = multipartMessageParts.get("messageWithToken").toString();
		}
		String header = multipartMessageParts.get("header").toString();
		String payload = null;
		if(multipartMessageParts.containsKey("payload")) {
			payload = multipartMessageParts.get("payload").toString();
		}
		
		String forwardTo = headesParts.get("Forward-To").toString();
		Message message = multiPartMessageServiceImpl.getMessage(header);
		
		CloseableHttpResponse response = null;
		if(isEnabledIdscp) {
			// -- Send data using IDSCP - (Client) - WebSocket
			sendMultipartMessageWebSocket(header, payload, forwardTo);
		}else {
			// -- Send message using HTTPS
			if(Boolean.parseBoolean(headesParts.get("Is-Enabled-Daps-Interaction").toString())) {
				response = forwardMessageBinary(forwardTo, messageWithToken, payload);
			} else {
				response = forwardMessageBinary(forwardTo, header, payload);
			}
		}
		
		// Handle response
		handleResponse(exchange, message, response, forwardTo);
		
		if(response!=null) {
			response.close();
		}
		
	}
	
	private CloseableHttpResponse forwardMessageBinary(String address, String header, String payload) throws UnsupportedEncodingException {
		logger.info("Forwarding Message: Body: form-data");
		
		// Covert to ContentBody
		ContentBody cbHeader = convertToContentBody(header, ContentType.DEFAULT_TEXT, "header");
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
	
	private ContentBody convertToContentBody(String value, ContentType contentType, String valueName) throws UnsupportedEncodingException {
		byte[] valueBiteArray = value.getBytes("utf-8");
		ContentBody cbValue = new ByteArrayBody(valueBiteArray, contentType, valueName);
		return cbValue;
	}
	
	private CloseableHttpClient getHttpClient() {
		AcceptAllTruststoreConfig config=new AcceptAllTruststoreConfig();

		CloseableHttpClient httpClient = HttpClientGenerator.get(config);
		logger.warn("Created Accept-All Http Client");

		return httpClient;
	}
	
	private void handleResponse(Exchange exchange, Message message, CloseableHttpResponse response, String forwardTo) throws UnsupportedOperationException, IOException {
		if (response==null) {
			logger.info("...communication error");
			rejectionMessageServiceImpl.sendRejectionMessage(
					RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, 
					message);
		} else {
			String responseString=new String(response.getEntity().getContent().readAllBytes());
			logger.info("response received from the DataAPP="+responseString);
			
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("status code of the response message is: " + statusCode);
			if (statusCode >=300) { 
				logger.info("data sent to destination "+forwardTo);
				rejectionMessageServiceImpl.sendRejectionMessage(
						RejectionMessageType.REJECTION_MESSAGE_COMMON, 
						message);
			}else {
				logger.info("data sent to destination "+forwardTo);
				logger.info("Successful response: "+ responseString);
				exchange.getOut().setBody(responseString);
			}
		}
	}

	private void sendMultipartMessageWebSocket(String header, String payload, String forwardTo) throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
		// Create idscpClient
		IdscpClientBean idscpClientBean = webSocketConfiguration.idscpClientServiceSinelton();
		IdscpClient idscpClient = idscpClientBean.getClient();
		// Create multipartMessage as a String
		HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(header, payload, null);
		String multipartMessage = EntityUtils.toString(entity, "UTF-8");
		// Send multipartMessage as a Frames
		FileStreamingBean fileStreamingBean = webSocketConfiguration.fileStreamingBeanWebSocket();
		// Extract IP and Port from the Forward-To (idscp://localhost:8081)
		String serverIp = forwardTo.substring(8, forwardTo.indexOf(":", 8)); 
		int serverPort = Integer.parseInt(forwardTo.substring(forwardTo.indexOf(":", 8)+1));
		// Send multipartmessage as frames
		fileStreamingBean.sendMultipartMessage(idscpClient, multipartMessage, serverIp, serverPort);
	}
}
