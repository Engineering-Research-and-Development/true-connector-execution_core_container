package it.eng.idsa.businesslogic.processor.receiver;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.FileRecreatorBeanServer;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
@ConditionalOnExpression(
		"${application.websocket.isEnabled:true}"
)
public class ReceiverFileRecreatorProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ReceiverFileRecreatorProcessor.class);

	@Autowired
	private WebSocketServerConfigurationB webSocketServerConfiguration;

	@Autowired
	private RejectionMessageService rejectionMessageService;


	@Override
	public void process(Exchange exchange) throws Exception {

		Message message=null;
		//  Receive and recreate Multipart message
		FileRecreatorBeanServer fileRecreatorBean = webSocketServerConfiguration.fileRecreatorBeanWebSocket();
		this.initializeServer(message, fileRecreatorBean);
		Thread fileRecreatorBeanThread = new Thread(fileRecreatorBean, "FileRecreator_"+this.getClass().getSimpleName());
		fileRecreatorBeanThread.start();
		String recreatedMultipartMessage = webSocketServerConfiguration.recreatedMultipartMessageBeanWebSocket().remove();
		logger.debug("Received message over WSS , {}", recreatedMultipartMessage);
		
		exchange.getMessage().setBody(recreatedMultipartMessage, String.class);
	}

	private void initializeServer(Message message, FileRecreatorBeanServer fileRecreatorBean) {
		try {
			fileRecreatorBean.setup();
		} catch(Exception e) {
			logger.info("... can not initilize the IdscpServer");
			logger.error(e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		}
	}

}

