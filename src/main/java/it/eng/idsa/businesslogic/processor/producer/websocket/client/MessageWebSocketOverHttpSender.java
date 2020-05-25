package it.eng.idsa.businesslogic.processor.producer.websocket.client;

import static org.asynchttpclient.Dsl.asyncHttpClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;

import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.SslEngineFactory;
import org.asynchttpclient.netty.ssl.JsseSslEngineFactory;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;
import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.HttpWebSocketServerBean;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.service.MultipartMessageTransformerService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * @author Antonio Scatoloni
 */

@Component
public class MessageWebSocketOverHttpSender {
    private static final Logger logger = LogManager.getLogger(ProducerSendDataToBusinessLogicProcessor.class);

    @Autowired
    private WebSocketClientConfiguration webSocketClientConfiguration;

    @Autowired
    private RejectionMessageService rejectionMessageService;
    
    @Autowired
    MultipartMessageTransformerService multipartMessageTransformerService;

    public String sendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String header, String payload)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, null, header, payload, null);
    }

    public String sendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String header, String payload, Message message)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort,null, header, payload, message);
    }

    public String sendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String webSocketPath, String header, String payload)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, webSocketPath, header, payload, null);
    }

    public String sendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String webSocketPath, String header, String payload, Message message)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, header, webSocketPath,  payload, message);
    }


    private String doSendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String webSocketPath, String header, String payload, Message message)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
    	
    	MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(header)
    			.withPayloadContent(payload)
    			.build();
    	//TODO Use this implementation with includeHttpHeaders set to false, but in future implementations these headers may be mandatory
    	String multipartMessageString = multipartMessageTransformerService.multipartMessagetoString(multipartMessage, false);
    													                                                        
        FileStreamingBean fileStreamingBean = webSocketClientConfiguration.fileStreamingWebSocket();
        WebSocket wsClient = createWebSocketClient(webSocketHost, webSocketPort, webSocketPath, message);
        // Try to connect to the Server. Wait until you are not connected to the server.
        fileStreamingBean.setup(wsClient);
        fileStreamingBean.sendMultipartMessage(multipartMessageString);
        // We don't have status of the response (is it 200 OK or not). We have only the content of the response.
        String responseMessage = new String(webSocketClientConfiguration.responseMessageBufferWebSocketClient().remove());
        closeWSClient(wsClient, message);
        logger.info("received response: " + responseMessage);

        return responseMessage;
    }

    @NotNull
    private WebSocket createWebSocketClient(String webSocketHost, Integer webSocketPort, String webSocketPath, Message message) {
        String WS_URL = "wss://" + webSocketHost + ":" + webSocketPort + ( webSocketPath == null ? HttpWebSocketServerBean.WS_URL : webSocketPath );
        WebSocket wsClient = null;
        try {
            final SslEngineFactory ssl = getSslEngineFactory();

            DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                    .setDisableHttpsEndpointIdentificationAlgorithm(true)
                    .setUseOpenSsl(true)
                    .setSslEngineFactory(ssl)
                    .build();

            WebSocketUpgradeHandler.Builder upgradeHandlerBuilder
                    = new WebSocketUpgradeHandler.Builder();
            WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder
                    .addWebSocketListener(webSocketClientConfiguration.inputStreamSocketListenerWebSocketClient()).build();
            wsClient = asyncHttpClient(clientConfig)
                    .prepareGet(WS_URL)
                    .execute(wsHandler)
                    .get();
            return wsClient;
        } catch (Exception e) {
            logger.info("... can not create the WebSocket connection HTTP at: " + WS_URL);
            if (null != message)
                rejectionMessageService.sendRejectionMessage(
                        RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                        message);
        }
        return wsClient;
    }

    @NotNull
    private SslEngineFactory getSslEngineFactory() throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                }
        };
        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return new JsseSslEngineFactory(sslContext);
    }


    private void closeWSClient(WebSocket wsClient, Message message) {
        // Send the close frame 1000 (CLOSE), "Shutdown"; in this method we also close the wsClient.
        try {
            wsClient.sendCloseFrame(1000, "Shutdown");
        } catch (Exception e) {
            logger.error("Problems encountered during Client Shutdown with error: " + e.getMessage());
            if (null != message)
                rejectionMessageService.sendRejectionMessage(
                        RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                        message);
        }
    }

}
