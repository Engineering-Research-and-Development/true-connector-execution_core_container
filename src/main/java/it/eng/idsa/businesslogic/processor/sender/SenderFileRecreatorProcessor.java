package it.eng.idsa.businesslogic.processor.sender;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.FileRecreatorBeanServer;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketMessagingLogicA;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
@ConditionalOnProperty(
		value="application.dataApp.websocket.isEnabled",
		havingValue = "true",
		matchIfMissing = false)
public class SenderFileRecreatorProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(SenderFileRecreatorProcessor.class);
	
	@Autowired
	private WebSocketServerConfigurationA webSocketServerConfiguration;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	@CamelAuditable(successEventType = TrueConnectorEventType.CONNECTOR_REQUEST, 
	failureEventType = TrueConnectorEventType.EXCEPTION_BAD_REQUEST)
	public void process(Exchange exchange) throws Exception {
		
		MultipartMessage multipartMessage = null;

		//  Receive and recreate Multipart message
		FileRecreatorBeanServer fileRecreatorBean = webSocketServerConfiguration.fileRecreatorBeanWebSocket();
		this.initializeServer(fileRecreatorBean);
		Thread fileRecreatorBeanThread = new Thread(fileRecreatorBean, "FileRecreator_"+this.getClass().getSimpleName());
		fileRecreatorBeanThread.start();
		String recreatedMultipartMessage = webSocketServerConfiguration.recreatedMultipartMessageBeanWebSocket().remove();
		
		// Extract header and payload from the multipart message
		try {
			multipartMessage = MultipartMessageProcessor.parseMultipartMessage(recreatedMultipartMessage);
		} catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
		}
		
		//String wsURI = "wss://0.0.0.0:8086"+ HttpWebSocketServerBean.WS_URL;
		if (null == exchange.getMessage().getHeader("Forward-To")) {
			exchange.getMessage().setHeader("Forward-To", HttpWebSocketMessagingLogicA.getInstance().getForwardTo());
		}
		
		// Return exchange
		exchange.getMessage().setBody(multipartMessage);
	}

	private void initializeServer(FileRecreatorBeanServer fileRecreatorBean) {
		try {
			fileRecreatorBean.setup();
		} catch(Exception e) {
			logger.info("... can not initilize the IdscpServer");
			rejectionMessageService.sendRejectionMessage(null, RejectionReason.TEMPORARILY_NOT_AVAILABLE);
		}
	}

}
