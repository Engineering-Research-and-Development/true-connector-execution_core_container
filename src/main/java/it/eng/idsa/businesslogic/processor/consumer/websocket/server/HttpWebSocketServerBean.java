package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


import java.io.IOException;
import java.net.BindException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PreDestroy;

/**
 * Jetty Server instantiation with WebSocket over SSL
 *
 * @author Antonio Scatoloni
 */
public class HttpWebSocketServerBean {
    private static final Logger logger = LogManager.getLogger(HttpWebSocketServerBean.class);
    public static final String WS_URL = "/incoming-data-channel-received-message";

    @Value("${application.idscp.server.port}")
    private int idscpServerPort;

    @Value("${server.ssl.key-store}")
    private String keyStoreLocation;

    @Value("${server.ssl.key-password}")
    private String keyStorePassword;

    private Server server;

    @Autowired
    private ResourceLoader resourceLoader;

    public Server createServer() {
        if (null == server || !server.isStarted() || !server.isRunning()) {
            try {
                setup();
                start();
            } catch (Exception e) {
                logger.error("Error on starting JETTY Server with stack: " + e.getMessage());
            }
        }
        return server;
    }

    public void setup() throws IOException {
        Resource resourceKeyStore = resourceLoader.getResource(keyStoreLocation);
        String keyStore = resourceKeyStore.getFile().getAbsolutePath();
        Path keystorePath = Paths.get(keyStore);
        String password = keyStorePassword;

        int port = idscpServerPort; //SECURE_PORT;

        HttpConfiguration http_config = getHttpConfiguration(port);
        SslContextFactory sslContextFactory = getSslContextFactory(keystorePath, password);
        HttpConfiguration https_config = new HttpConfiguration(http_config);

        server = new Server();
        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory,
                        HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(https_config));
        connector.setPort(port);
        //connector.setReuseAddress(true);
        server.addConnector(connector);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(HttpWebSocketMessagingServlet.class, WS_URL);
        server.setHandler(handler);
    }

    public void start() {
        try {
            server.start();
            //server.join();
        } catch (BindException e) {
            logger.warn("IDSCP Server should be 'OFF' in order to use WS over HTTPS!");
            logger.warn(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void stop() throws Exception {
        if (null != server && (server.isStarted() || server.isRunning())) {
            server.stop();
            server = null;
        }
    }

    @NotNull
    private SslContextFactory getSslContextFactory(Path keystorePath, String password) {
        SslContextFactory sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath.toAbsolutePath().toString());
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

}