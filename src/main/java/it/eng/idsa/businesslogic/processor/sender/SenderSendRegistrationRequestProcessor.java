package it.eng.idsa.businesslogic.processor.sender;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
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

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.communication.HttpClientGenerator;
import it.eng.idsa.businesslogic.util.config.keystore.AcceptAllTruststoreConfig;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Component
public class SenderSendRegistrationRequestProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderSendRegistrationRequestProcessor.class);

	@Autowired
    private MultipartMessageService multipartMessageService;
	
    @Autowired
    private RejectionMessageService rejectionMessageService;
	   
	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> headesParts = exchange.getMessage().getHeaders();
		Map<String, Object> multipartMessageParts = exchange.getMessage().getBody(HashMap.class);
		String forwardTo = headesParts.get("Forward-To").toString();

		logger.info("About to send request towards broker... '{}'", forwardTo);
		
		String header = multipartMessageParts.get("header").toString();
		String payload = null;
		if (multipartMessageParts.containsKey("payload")) {
			payload = multipartMessageParts.get("payload").toString();
		}
		
		Message message = multipartMessageService.getMessage(header);
        MultipartMessage multipartMessage = new MultipartMessageBuilder()
//    			.withHeaderContent(header)
        		.withHeaderContent(message)
    			.withPayloadContent(payload)
    			.build();
        String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage);


		CloseableHttpResponse response = this.sendMultipartMessage(headesParts, header, payload, forwardTo);
		// Handle response
//		this.handleResponse(exchange, message, response, forwardTo, multipartMessageString);

//		String responseString = new String(response.getEntity().getContent().readAllBytes());
//		logger.info("response received from the DataAPP=" + responseString);

        // Handle response
        this.handleResponse(exchange, message, response, forwardTo, multipartMessageString);

		if (response != null) {
			response.close();
		}
	}

	private CloseableHttpResponse sendMultipartMessage(Map<String, Object> headesParts, String header, String payload,
			String forwardTo) throws IOException, KeyManagementException, NoSuchAlgorithmException,
			InterruptedException, ExecutionException, UnsupportedEncodingException {
		CloseableHttpResponse response = null;
		response = forwardMessageBinary(forwardTo, header, payload);
		return response;
	}

	private CloseableHttpResponse forwardMessageBinary(String address, String header, String payload)
			throws UnsupportedEncodingException {
		logger.info("Forwarding Message: Body: binary");

		// Covert to ContentBody
		ContentBody cbHeader = this.convertToContentBody(header, ContentType.APPLICATION_JSON, "header");
		ContentBody cbPayload = null;
		if (payload != null) {
			cbPayload = convertToContentBody(payload, ContentType.APPLICATION_JSON, "payload");
		}

		// Set F address
		HttpPost httpPost = new HttpPost(address);

		HttpEntity reqEntity = payload == null ? 
				MultipartEntityBuilder.create().addPart("header", cbHeader).build()
				: MultipartEntityBuilder.create().addPart("header", cbHeader).addPart("payload", cbPayload)
				.build();

		httpPost.setEntity(reqEntity);

		CloseableHttpResponse response;
		try {
			response = getHttpClient().execute(httpPost);
		} catch (IOException e) {
			logger.error(e);
			return null;
		}

		return response;
	}

	private ContentBody convertToContentBody(String value, ContentType contentType, String valueName)
			throws UnsupportedEncodingException {
		byte[] valueBiteArray = value.getBytes("utf-8");
		ContentBody cbValue = new ByteArrayBody(valueBiteArray, contentType, valueName);
		return cbValue;
	}

	private CloseableHttpClient getHttpClient() throws IOException {
		AcceptAllTruststoreConfig config = new AcceptAllTruststoreConfig();

		CloseableHttpClient httpClient = HttpClientGenerator.get(config, true);
		logger.warn("Created Accept-All Http Client");
		/*
		logger.info("Creating custom http client with broker certificate");
		InputStream is = null;
		try {
			is = new ClassPathResource("broker_cert.cer").getInputStream();

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null); // You don't need the KeyStore instance to come from a file.
			ks.setCertificateEntry("caCert", caCert);

			tmf.init(ks);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);

			SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslContext,
					new NoopHostnameVerifier());

			// Creating HttpClientBuilder
			HttpClientBuilder clientbuilder = HttpClients.custom();

			// Setting the SSLConnectionSocketFactory
			clientbuilder = clientbuilder.setSSLSocketFactory(sslConSocFactory);

			// Building the CloseableHttpClient
			CloseableHttpClient httpClient = clientbuilder.build();
		 */
			return httpClient;
//		} catch(CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException |
//				KeyManagementException ex) {
//			logger.error("Exception {}", ex);
//		}
//		finally {
//			if (is != null) {
//				is.close();
//			}
//		}
//		return null;
	}

	private void handleResponse(Exchange exchange, Message message, CloseableHttpResponse response, String forwardTo, String multipartMessageBody) throws UnsupportedOperationException, IOException {
        if (response == null) {
            logger.info("...communication error");
            rejectionMessageService.sendRejectionMessage(
                    RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                    message);
        } else {
            String responseString = new String(response.getEntity().getContent().readAllBytes());
            logger.info("response received from the DataAPP=" + responseString);

            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("status code of the response message is: " + statusCode);
            if (statusCode >= 300) {
                if (statusCode == 404) {
                    logger.info("...communication error - bad forwardTo URL" + forwardTo);
                    rejectionMessageService.sendRejectionMessage(
                            RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                            message);
                }
                logger.info("data sent unuccessfully to destination " + forwardTo);
                rejectionMessageService.sendRejectionMessage(
                        RejectionMessageType.REJECTION_MESSAGE_COMMON,
                        message);
            } else {
                logger.info("data sent to destination " + forwardTo);
                logger.info("Successful response: " + responseString);
                // TODO:
                // Set original body which is created using the original payload and header
				exchange.getMessage().setHeader("header", multipartMessageService.getHeaderContentString(responseString));
				String payload = multipartMessageService.getPayloadContent(responseString);
				if (StringUtils.isEmpty(payload)) {
					payload = "Empty";
				}
				exchange.getMessage().setHeader("payload", payload);
                exchange.getMessage().setBody(responseString);
            }
        }
    }
}
