package it.eng.idsa.businesslogic.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import it.eng.idsa.businesslogic.processor.receiver.websocket.server.FileRecreatorBeanServer;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.FrameBufferBean;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketMessagingLogicB;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketMessagingServletB;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketServerBean;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.RecreatedMultipartMessageBean;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.ResponseMessageBufferBean;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.ResponseMessageSendPartialServer;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@Configuration
@ConditionalOnExpression("${application.websocket.isEnabled:true}")
public class WebSocketServerConfigurationB implements WebSocketServerConfiguration {

	@Value("${application.wss-server-port}")
	private int port;

	@Override
	@Bean(name = "frameBufferWebSocketB")
	@Scope("singleton")
	@Qualifier(value = "FrameBufferBeanB")
	public FrameBufferBean frameBufferWebSocket() {
		return new FrameBufferBean();
	}

	/**
	 * @author Antonio Scatoloni
	 * @return HttpWebSocketServerBean
	 */

	@Override
	@Bean(name = "httpsServerWebSocketB")
	@Scope("singleton")
	@Qualifier(value = "HttpWebSocketServerBeanB")
	public HttpWebSocketServerBean httpsServerWebSocket() {
		HttpWebSocketServerBean httpWebSocketServerBean = new HttpWebSocketServerBean();
		httpWebSocketServerBean.setPort(port);
		httpWebSocketServerBean.setMessagingServlet(HttpWebSocketMessagingServletB.class);
		return httpWebSocketServerBean;
	}

	/**
	 * @author Antonio Scatoloni
	 * @return HttpWebSocketMessagingLogicB
	 */

	@Bean(name = "messagingLogicB")
	@Scope("singleton")
	@Qualifier(value = "MessagingLogicB")
	public HttpWebSocketMessagingLogicB messagingLogic() {
		HttpWebSocketMessagingLogicB httpWebSocketMessagingLogic = HttpWebSocketMessagingLogicB.getInstance();
		httpWebSocketMessagingLogic.setWebSocketServerConfiguration(this);
		return httpWebSocketMessagingLogic;
	}

	@Override
	@Bean(name = "fileRecreatorBeanWebSocketB")
	@Scope("singleton")
	@Qualifier(value = "FileRecreatorBeanServerB")
	public FileRecreatorBeanServer fileRecreatorBeanWebSocket() {
		FileRecreatorBeanServer fileRecreatorBeanServer = new FileRecreatorBeanServer();
		fileRecreatorBeanServer.setWebSocketServerConfiguration(this);
		return fileRecreatorBeanServer;
	}

	@Override
	@Bean(name = "recreatedMultipartMessageBeanWebSocketB")
	@Scope("singleton")
	@Qualifier(value = "RecreatedMultipartMessageBeanB")
	public RecreatedMultipartMessageBean recreatedMultipartMessageBeanWebSocket() {
		return new RecreatedMultipartMessageBean();
	}

	@Override
	@Bean(name = "responseMessageBufferWebSocketB")
	@Scope("singleton")
	@Qualifier(value = "ResponseMessageBufferBeanB")
	public ResponseMessageBufferBean responseMessageBufferWebSocket() {
		return new ResponseMessageBufferBean();
	}

	@Override
	@Bean(name = "responseMessageSendPartialWebSocketB")
	@Scope("singleton")
	@Qualifier(value = "ResponseMessageSendPartialServerB")
	public ResponseMessageSendPartialServer responseMessageSendPartialWebSocket() {
		ResponseMessageSendPartialServer responseMessageSendPartialServer = new ResponseMessageSendPartialServer();
		responseMessageSendPartialServer.setWebSocketServerConfiguration(this);
		return responseMessageSendPartialServer;
	}

}
