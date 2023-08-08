package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.security.KeyStore;

import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Jetty Server instantiation with WebSocket over SSL
 *
 * @author Antonio Scatoloni
 */
public class HttpWebSocketServerBean {
    private static final Logger logger = LoggerFactory.getLogger(HttpWebSocketServerBean.class);
    public static final String WS_URL = "/incoming-data-channel-received-message";
    private int port;
    private Class messagingServlet;

	@Value("${server.ssl.key-store-type}")
	private String keyStoreType;
    
    @Value("${server.ssl.key-store}")
    private String keyStoreLocation;

    @Value("${server.ssl.key-password}")
    private String keyStorePassword;

    private Server server;

    @Autowired
    private ResourceLoader resourceLoader;

    public synchronized Server createServer() {
        if (null == server
                || !server.isStarted()
                || !server.isRunning()
                 ){
            try {
                setup();
                start();
            } catch (Exception e) {
                logger.error("Error on executing JETTY Server with stack: " + e.getMessage());
            }
        }
        return server;
    }

    public void setup() throws IOException {
    	// Prepare keystore
    	InputStream keyStore=null;
    	try {
    			Resource resourceKeyStore = resourceLoader.getResource(keyStoreLocation);
    			keyStore = resourceKeyStore.getInputStream();
    		}
    	catch (FileNotFoundException e) {
    			keyStore = new FileInputStream(keyStoreLocation);
    		}
    	try {		 
    		final KeyStore ks = KeyStore.getInstance(keyStoreType);
    		ks.load(keyStore, keyStorePassword.toCharArray());

            int port = getPort();

    		String password = keyStorePassword;
    		HttpConfiguration http_config = getHttpConfiguration(port);
    		SslContextFactory sslContextFactory = getSslContextFactory (ks, password);
    		HttpConfiguration https_config = new HttpConfiguration(http_config);

    		server = new Server();
    		ServerConnector connector = new ServerConnector(server,
    				new SslConnectionFactory(sslContextFactory,
    						HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(https_config));
    		connector.setPort(port);
    		//connector.setReuseAddress(true);
    		server.addConnector(connector);

            HandlerCollection handlerCollection = new HandlerCollection();

            ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            handler.setContextPath("/");
            handler.addServlet(getMessagingServlet(), WS_URL);
            handlerCollection.setHandlers(new Handler[]{handler});

            server.setHandler(handlerCollection);
    	}catch (Exception e) {
			logger.error("Failed to start websocket server: {}", e.getMessage());
    	}
    }

    public void start() {
        try {
            server.start();
            //server.join();
        } catch (BindException e) {
            logger.warn("IDSCP Server should be 'OFF' in order to use WS over HTTPS!");
            logger.warn(e.getMessage());
        } catch (Exception e) {
            logger.error("WebSocket Server did not start: {}", e.getMessage());
        }
    }

    public void stop() throws Exception {
        if (null != server && (server.isStarted() || server.isRunning())) {
            server.stop();
            server = null;
        }
    }

    @NotNull
    private SslContextFactory getSslContextFactory(KeyStore keystore, String password) {
        SslContextFactory sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStore(keystore);
        sslContextFactory.setKeyStorePassword(password);
        sslContextFactory.setKeyManagerPassword(password);
        return sslContextFactory;
    }

    @NotNull
    private HttpConfiguration getHttpConfiguration(int port) {
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(port);
        http_config.addCustomizer(new SecureRequestCustomizer());
        return http_config;
    }
    
	@PreDestroy
    public void onDestroy() throws Exception {
        if (server.getServer()!=null) {
        	server.getServer().stop();
        	server.getServer().destroy();
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Class getMessagingServlet() {
        return messagingServlet;
    }

    public void setMessagingServlet(Class messagingServlet) {
        this.messagingServlet = messagingServlet;
    }
}