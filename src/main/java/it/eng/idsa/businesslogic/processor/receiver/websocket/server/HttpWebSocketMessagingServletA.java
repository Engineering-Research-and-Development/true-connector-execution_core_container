package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
/**
 * Well, it's just a servlet declaration.
 * @author Antonio Scatoloni
 */
public class HttpWebSocketMessagingServletA extends WebSocketServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public void configure(WebSocketServletFactory factory) {
       factory.register(HttpWebSocketListenerServerA.class);
    }

}