package it.eng.idsa.businesslogic.processor.sender;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.FileRecreatorBeanServer;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;

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
	
	private static final Logger logger = LogManager.getLogger(SenderFileRecreatorProcessor.class);
	
	@Autowired
	private WebSocketServerConfigurationA webSocketServerConfiguration;
	
	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
public void process(Exchange exchange) throws Exception {
		
		String header;
		String payload;
		Message message=null;
		Map<String, Object> multipartMessageParts = new HashMap<String, Object>();
		MultipartMessage multipartMessage = null;

		//  Receive and recreate Multipart message
		FileRecreatorBeanServer fileRecreatorBean = webSocketServerConfiguration.fileRecreatorBeanWebSocket();
		this.initializeServer(message, fileRecreatorBean);
		Thread fileRecreatorBeanThread = new Thread(fileRecreatorBean, "FileRecreator_"+this.getClass().getSimpleName());
		fileRecreatorBeanThread.start();
		String recreatedMultipartMessage = webSocketServerConfiguration.recreatedMultipartMessageBeanWebSocket().remove();
		
		// Extract header and payload from the multipart message
		try {
			header = multipartMessageService.getHeaderContentString(recreatedMultipartMessage);
			multipartMessageParts.put("header", header);
			payload = multipartMessageService.getPayloadContent(recreatedMultipartMessage);
			if(payload!=null) {
				multipartMessageParts.put("payload", payload);
			}
			Message msg = multipartMessageService.getMessage(header);
			multipartMessage = new MultipartMessage(
					null, null, msg, null, payload, null, null,null);
		} catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			// TODO: Send WebSocket rejection message
			
		}
		// Return exchange
		exchange.getMessage().setHeaders(multipartMessageParts);
		exchange.getMessage().setBody(multipartMessage);
	}

	private void initializeServer(Message message, FileRecreatorBeanServer fileRecreatorBean) {
		try {
			fileRecreatorBean.setup();
		} catch(Exception e) {
			logger.info("... can not initilize the IdscpServer");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, 
					message);
		}
	}

}
