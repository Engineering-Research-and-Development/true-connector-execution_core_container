package it.eng.idsa.businesslogic.processor.producer.websocket.client;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.HttpWebSocketServerBean;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import nl.tno.ids.common.multipart.MultiPartMessage;
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.SslEngineFactory;
import org.asynchttpclient.netty.ssl.JsseSslEngineFactory;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/**
 * @author Antonio Scatoloni
 */

@Component
public class MessageWebSocketOverHttpSender {
    private static final Logger logger = LogManager.getLogger(ProducerSendDataToBusinessLogicProcessor.class);
    public static final String REGEX_WSS = "(wss://)([^:^/]*)(:)(\\d*)";


    @Autowired
    private WebSocketClientConfiguration webSocketClientConfiguration;

    @Autowired
    private RejectionMessageServiceImpl rejectionMessageServiceImpl;

    @Value("${application.idscp.server.port}")
    private int idscpServerPort;

    private int webSocketPort;
    private String webSocketHost;

    public String sendMultipartMessageWebSocketOverHttps(String header, String payload, String forwardTo)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(header, payload, forwardTo, null);
    }

    public String sendMultipartMessageWebSocketOverHttps(String header, String payload, String forwardTo, Message message)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        return doSendMultipartMessageWebSocketOverHttps(header, payload, forwardTo, message);
    }

    private String doSendMultipartMessageWebSocketOverHttps(String header, String payload, String forwardTo, Message message)
            throws ParseException, IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
        MultiPartMessage multipartMessage = new MultiPartMessage.Builder()
                .setHeader(header)
                .setPayload(payload)
                .build();
        extractWebSocketIPAndPort(forwardTo);
        FileStreamingBean fileStreamingBean = webSocketClientConfiguration.fileStreamingWebSocket();
        WebSocket wsClient = createWebSocketClient(message);
        // Try to connect to the Server. Wait until you are not connected to the server.
        fileStreamingBean.setup(wsClient);
        fileStreamingBean.sendMultipartMessage(multipartMessage.toString());
        //fileStreamingBean.sendMultipartMessage(multipartMessage);
        // We don't have status of the response (is it 200 OK or not). We have only the content of the response.
        String responseMessage = new String(webSocketClientConfiguration.responseMessageBufferWebSocketClient().remove());
        closeWSClient(wsClient, message);
        logger.info("received response: " + responseMessage);

        return responseMessage;
    }

    @NotNull
    private WebSocket createWebSocketClient(Message message) {
        String WS_URL = "wss://" + webSocketHost + ":" + webSocketPort + HttpWebSocketServerBean.WS_URL;
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
            logger.info("... can not create the WebSocket connection HTTP");
            if (null != message)
                rejectionMessageServiceImpl.sendRejectionMessage(
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
                rejectionMessageServiceImpl.sendRejectionMessage(
                        RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                        message);
        }
    }

    private void extractWebSocketIPAndPort(String forwardTo) {
        //Example of Forward-to : https://localhost:8889/incoming-data-channel/receivedMessage
        URL senderURL = null;
        try {
            senderURL = new URL(forwardTo);
            webSocketPort = senderURL.getPort();
            webSocketHost = senderURL.getHost();
        } catch (MalformedURLException e) {
            //Example of Forward-to : wss://localhost:8086
            logger.info("Use IDSCP port for WS over https!");
            Pattern pattern = Pattern.compile(REGEX_WSS);
            Matcher matcher = pattern.matcher(forwardTo);
            matcher.find();
            this.webSocketHost = matcher.group(2);
            this.webSocketPort = Integer.parseInt(matcher.group(4));
        }
    }

}
