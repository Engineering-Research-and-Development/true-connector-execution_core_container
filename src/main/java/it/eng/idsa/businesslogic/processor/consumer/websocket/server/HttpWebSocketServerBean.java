package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.net.BindException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Jetty Server instantiation with WebSocket over SSL
 * @author Antonio Scatoloni
 */
public class HttpWebSocketServerBean {
    private static final Logger logger = LogManager.getLogger(HttpWebSocketServerBean.class);
    public static final String WS_URL = "/incoming-received-data-ws";
    public static final int SECURE_PORT = 8891;

    @Value("${application.idscp.server.port}")
    private int idscpServerPort;

    private Server server;

    public Server createServer() {
        if (null == server) {
            setup();
            start();
            return server;
        }
        return null;
    }

    public void setup()  {
        final Path jssePath = FileSystems.getDefault().getPath("src/main/resources/jsse");
        Path keystorePath = jssePath.resolve("server-keystore.jks");
        Path trustStorePath = jssePath.resolve("server-truststore.jks");
        String password = "password";

        int port = idscpServerPort; //SECURE_PORT;

        HttpConfiguration http_config = getHttpConfiguration(port);
        SslContextFactory sslContextFactory = getSslContextFactory(keystorePath, trustStorePath, password);
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
            logger.warn(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void stop() throws Exception {
        if (null != server && (server.isRunning() || server.isStarted())) {
            server.stop();
            server = null;
        }
    }

    @NotNull
    private SslContextFactory getSslContextFactory(Path keystorePath, Path trustStorePath, String password) {
        SslContextFactory sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath.toAbsolutePath().toString());
        sslContextFactory.setKeyStorePassword(password);
        sslContextFactory.setKeyManagerPassword(password);
        sslContextFactory.setTrustStorePath(trustStorePath.toAbsolutePath().toString());
        sslContextFactory.setTrustStorePassword(password);
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

}