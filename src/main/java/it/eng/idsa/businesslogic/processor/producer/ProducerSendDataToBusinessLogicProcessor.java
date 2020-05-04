package it.eng.idsa.businesslogic.processor.producer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.ws.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fhg.aisec.ids.comm.client.IdscpClient;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;
import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.FileStreamingBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.IdscpClientBean;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.MessageWebSocketOverHttpSender;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.MultipartMessageTransformerService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.communication.HttpClientGenerator;
import it.eng.idsa.businesslogic.util.config.keystore.AcceptAllTruststoreConfig;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@Component
public class ProducerSendDataToBusinessLogicProcessor implements Processor {

    private static final Logger logger = LogManager.getLogger(ProducerSendDataToBusinessLogicProcessor.class);
    // example for the webSocketURL: idscp://localhost:8099
    public static final String REGEX_IDSCP = "(idscp://)([^:^/]*)(:)(\\d*)";
    public static final String REGEX_WSS = "(wss://)([^:^/]*)(:)(\\d*)";

    @Value("${application.idscp.isEnabled}")
    private boolean isEnabledIdscp;

    @Value("${application.websocket.isEnabled}")
    private boolean isEnabledWebSocket;

    @Autowired
    private MultipartMessageService multipartMessageService;

    @Autowired
    private RejectionMessageService rejectionMessageService;

    @Autowired
    private WebSocketClientConfiguration webSocketClientConfiguration;

    @Autowired
    private MessageWebSocketOverHttpSender messageWebSocketOverHttpSender;
    
    @Autowired
    MultipartMessageTransformerService multipartMessageTransformerService;

    private String webSocketHost;
    private Integer webSocketPort;

    @Override
    public void process(Exchange exchange) throws Exception {

        Map<String, Object> headesParts = exchange.getIn().getHeaders();
        Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);

        String messageWithToken = null;

        // Get parts of the Multipart message
        if (Boolean.parseBoolean(headesParts.get("Is-Enabled-Daps-Interaction").toString())) {
            messageWithToken = multipartMessageParts.get("messageWithToken").toString();
        }
        String header = multipartMessageParts.get("header").toString();
        String payload = null;
        if (multipartMessageParts.containsKey("payload")) {
            payload = multipartMessageParts.get("payload").toString();
        }
        String forwardTo = headesParts.get("Forward-To").toString();
        Message message = multipartMessageService.getMessage(header);
        
        MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(header)
    			.withPayloadContent(payload)
    			.build();
        String multipartMessageString = multipartMessageTransformerService.multipartMessagetoString(multipartMessage);

        if (isEnabledIdscp) {
            // check & exstract IDSCP WebSocket IP and Port
            try {
                this.extractWebSocketIPAndPort(forwardTo, REGEX_IDSCP);
            } catch (Exception e) {
                logger.info("... bad idscp URL");
                rejectionMessageService.sendRejectionMessage(
                        RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                        message);
            }
            // -- Send data using IDSCP - (Client) - WebSocket
            String response;
            if (Boolean.parseBoolean(headesParts.get("Is-Enabled-Daps-Interaction").toString())) {
                response = this.sendMultipartMessageWebSocket(this.webSocketHost, this.webSocketPort, messageWithToken, payload, message);
            } else {
                response = this.sendMultipartMessageWebSocket(this.webSocketHost, this.webSocketPort, header, payload, message);
            }
            // Handle response
            this.handleResponseWebSocket(exchange, message, response, forwardTo, multipartMessageString);
        } else if (isEnabledWebSocket) {
        	// check & exstract HTTPS WebSocket IP and Port
        	try {
                this.extractWebSocketIPAndPort(forwardTo, REGEX_WSS);
            } catch (Exception e) {
                logger.info("... bad wss URL");
                rejectionMessageService.sendRejectionMessage(
                        RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                        message);
            }
        	
        	// -- Send data using HTTPS - (Client) - WebSocket
            String response;
            if (Boolean.parseBoolean(headesParts.get("Is-Enabled-Daps-Interaction").toString())) {
                response = messageWebSocketOverHttpSender.sendMultipartMessageWebSocketOverHttps(this.webSocketHost, this.webSocketPort, messageWithToken, payload, message);
            } else {
                response = messageWebSocketOverHttpSender.sendMultipartMessageWebSocketOverHttps(this.webSocketHost, this.webSocketPort, header, payload, message);
            }
            // Handle response
            this.handleResponseWebSocket(exchange, message, response, forwardTo, multipartMessageString);
        } else {
            // Send MultipartMessage HTTPS
            CloseableHttpResponse response = this.sendMultipartMessage(
                    headesParts,
                    messageWithToken,
                    header,
                    payload,
                    forwardTo
            );
            // Handle response
            this.handleResponse(exchange, message, response, forwardTo, multipartMessageString);

            if (response != null) {
                response.close();
            }
        }

    }

    private CloseableHttpResponse sendMultipartMessage(
            Map<String, Object> headesParts,
            String messageWithToken,
            String header,
            String payload,
            String forwardTo) throws IOException, KeyManagementException,
            NoSuchAlgorithmException, InterruptedException, ExecutionException, UnsupportedEncodingException {
        CloseableHttpResponse response = null;
        // -- Send message using HTTPS
        if (Boolean.parseBoolean(headesParts.get("Is-Enabled-Daps-Interaction").toString())) {
            response = this.forwardMessageBinary(forwardTo, messageWithToken, payload);
        } else {
            response = forwardMessageBinary(forwardTo, header, payload);
        }
        return response;
    }

    private CloseableHttpResponse forwardMessageBinary(String address, String header, String payload) throws UnsupportedEncodingException {
        logger.info("Forwarding Message: Body: form-data");

        // Covert to ContentBody
        ContentBody cbHeader = this.convertToContentBody(header, ContentType.DEFAULT_TEXT, "header");
        ContentBody cbPayload = null;
        if (payload != null) {
            cbPayload = convertToContentBody(payload, ContentType.DEFAULT_TEXT, "payload");
        }

        // Set F address
        HttpPost httpPost = new HttpPost(address);

        HttpEntity reqEntity = payload == null ?
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
        AcceptAllTruststoreConfig config = new AcceptAllTruststoreConfig();

        CloseableHttpClient httpClient = HttpClientGenerator.get(config);
        logger.warn("Created Accept-All Http Client");

        return httpClient;
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
                exchange.getOut().setHeader("multipartMessageBody", multipartMessageBody);
                exchange.getOut().setBody(responseString);
            }
        }
    }

    private void handleResponseWebSocket(Exchange exchange, Message message, String responseString, String forwardTo, String multipartMessageBody) {
        if (responseString == null) {
            logger.info("...communication error");
            rejectionMessageService.sendRejectionMessage(
                    RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                    message);
        } else {
            logger.info("response received from the DataAPP=" + responseString);
            logger.info("data sent to destination " + forwardTo);
            logger.info("Successful response: " + responseString);
            // TODO:
            // Set original body which is created using the original payload and header 
            exchange.getOut().setHeader("multipartMessageBody", multipartMessageBody);
            exchange.getOut().setBody(responseString);
        }
    }

    private String sendMultipartMessageWebSocket(String webSocketHost, Integer webSocketPort, String header, String payload, Message message) throws Exception, ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        // Create idscpClient
        IdscpClientBean idscpClientBean = webSocketClientConfiguration.idscpClientServiceWebSocket();
        this.initializeIdscpClient(message, idscpClientBean);
        IdscpClient idscpClient = idscpClientBean.getClient();
        
        MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(header)
    			.withPayloadContent(payload)
    			.build();
    	String multipartMessageString = multipartMessageTransformerService.multipartMessagetoString(multipartMessage);

        // Send multipartMessage as a frames
        FileStreamingBean fileStreamingBean = webSocketClientConfiguration.fileStreamingWebSocket();
        WebSocket wsClient = this.createWebSocketConnection(idscpClient, webSocketHost, webSocketPort, message);
        // Try to connect to the Server. Wait until you are not connected to the server.
        wsClient.addWebSocketListener(webSocketClientConfiguration.inputStreamSocketListenerWebSocketClient());
        fileStreamingBean.setup(wsClient);
        fileStreamingBean.sendMultipartMessage(multipartMessageString);
        // We don't have status of the response (is it 200 OK or not). We have only the content of the response.
        String responseMessage = new String(webSocketClientConfiguration.responseMessageBufferWebSocketClient().remove());
        this.closeWSClient(wsClient);
        logger.info("received response: " + responseMessage);

        return responseMessage;
    }

    private void initializeIdscpClient(Message message, IdscpClientBean idscpClientBean) {
        try {
            idscpClientBean.createIdscpClient();
        } catch (Exception e) {
            logger.info("... can not initilize the IdscpClient");
            rejectionMessageService.sendRejectionMessage(
                    RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                    message);
        }
    }

    private WebSocket createWebSocketConnection(IdscpClient idscpClient, String webSocketHost, Integer webSocketPort, Message message) {
        WebSocket wsClient = null;
        try {
            wsClient = idscpClient.connect(webSocketHost, webSocketPort);
        } catch (Exception e) {
        	e.printStackTrace();
            logger.info("... can not create the WebSocket connection IDSCP");
            rejectionMessageService.sendRejectionMessage(
                    RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                    message);
        }
        return wsClient;
    }

    private void extractWebSocketIPAndPort(String forwardTo, String regexForwardTo) {
        // Split URL into protocol, host, port
        Pattern pattern = Pattern.compile(regexForwardTo);
        Matcher matcher = pattern.matcher(forwardTo);
        matcher.find();

        this.webSocketHost = matcher.group(2);
        this.webSocketPort = Integer.parseInt(matcher.group(4));
    }

    private void closeWSClient(WebSocket wsClient) {
        // Send the close frame 200 (OK), "Shutdown"; in this method we also close the wsClient.
        try {
            wsClient.sendCloseFrame(200, "Shutdown");
        } catch (Exception e) {
            //TODO: Handle rejection message
            e.printStackTrace();
        }
    }
}
