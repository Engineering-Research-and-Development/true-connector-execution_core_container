package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import static org.asynchttpclient.Dsl.asyncHttpClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLContext;
import javax.validation.constraints.NotNull;

import org.apache.http.ParseException;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.SslEngineFactory;
import org.asynchttpclient.netty.ssl.JsseSslEngineFactory;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketServerBean;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.businesslogic.service.impl.TLSProvider;

/**
 * Author: Antonio Scatoloni
 */

@Component
public class MessageWebSocketOverHttpSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageWebSocketOverHttpSender.class);

    private RejectionMessageService rejectionMessageService;
    private FileStreamingBean fileStreamingBean;
    private ResponseMessageBufferClient responseMessageBufferClient;
    private InputStreamSocketListenerClient inputStreamSocketListenerWebSocketClient;
    
    private TLSProvider tlsProvider;

    public MessageWebSocketOverHttpSender(RejectionMessageService rejectionMessageService, FileStreamingBean fileStreamingBean,
                                          ResponseMessageBufferClient responseMessageBufferClient, InputStreamSocketListenerClient inputStreamSocketListenerWebSocketClient,
                                          TLSProvider tlsProvider) {
        this.rejectionMessageService = rejectionMessageService;
        this.fileStreamingBean = fileStreamingBean;
        this.responseMessageBufferClient = responseMessageBufferClient;
        this.inputStreamSocketListenerWebSocketClient = inputStreamSocketListenerWebSocketClient;
        this.tlsProvider = tlsProvider;
    }

    public String sendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String header, String payload)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, null, header, payload, null);
    }

    public String sendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String header, String payload, Message message)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, null, header, payload, message);
    }

    public String sendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String webSocketPath, String header, String payload)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, webSocketPath, header, payload, null);
    }

    public String sendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String webSocketPath, String header, String payload, Message message)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, webSocketPath, header,  payload, message);
    }

    private String doSendMultipartMessageWebSocketOverHttps(String webSocketHost, Integer webSocketPort, String webSocketPath, String header, String payload, Message message)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
    	
    	MultipartMessage multipartMessage = new MultipartMessageBuilder()
    			.withHeaderContent(header)
    			.withPayloadContent(payload)
    			.build();
    	String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, Boolean.TRUE);

        WebSocket wsClient = createWebSocketClient(webSocketHost, webSocketPort, webSocketPath, message);
        fileStreamingBean.setup(wsClient);
        fileStreamingBean.sendMultipartMessage(multipartMessageString);
        String responseMessage = new String(responseMessageBufferClient.remove());
        closeWSClient(wsClient, message);
        logger.info("Response is received");

        return responseMessage;
    }

    @NotNull
    private WebSocket createWebSocketClient(String webSocketHost, Integer webSocketPort, String webSocketPath, Message message) {
        String WS_URL = "wss://" + webSocketHost + ":" + webSocketPort + ( webSocketPath == null ? HttpWebSocketServerBean.WS_URL : webSocketPath );
        WebSocket wsClient = null;
        try {
            final SslEngineFactory ssl = getSslEngineFactory();

            DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                    .setUseOpenSsl(true)
                    .setSslEngineFactory(ssl)
                    .build();

            WebSocketUpgradeHandler.Builder upgradeHandlerBuilder
                    = new WebSocketUpgradeHandler.Builder();
            WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder
                    .addWebSocketListener(inputStreamSocketListenerWebSocketClient).build();
            wsClient = asyncHttpClient(clientConfig)
                    .prepareGet(WS_URL)
                    .execute(wsHandler)
                    .get();
            return wsClient;
        } catch (Exception e) {
            logger.info("... can not create the WebSocket connection HTTP at '{}', {}", WS_URL, e);
            if (null != message)
                rejectionMessageService.sendRejectionMessage(message, RejectionReason.INTERNAL_RECIPIENT_ERROR);
        }
        return wsClient;
    }

    @NotNull
    private SslEngineFactory getSslEngineFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(tlsProvider.getKeyManagers(), tlsProvider.getTrustManagers(), new java.security.SecureRandom());
        return new JsseSslEngineFactory(sslContext);
    }

    private void closeWSClient(WebSocket wsClient, Message message) {
        try {
            wsClient.sendCloseFrame(1000, "Shutdown");
        } catch (Exception e) {
            logger.error("Problems encountered during Client Shutdown with error: " + e.getMessage());
            if (null != message)
                rejectionMessageService.sendRejectionMessage(message, RejectionReason.INTERNAL_RECIPIENT_ERROR);
        }
    }
}
