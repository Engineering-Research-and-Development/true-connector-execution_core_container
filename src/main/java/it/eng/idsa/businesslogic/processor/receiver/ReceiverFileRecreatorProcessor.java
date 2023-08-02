package it.eng.idsa.businesslogic.processor.receiver;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.FileRecreatorBeanServer;
import it.eng.idsa.businesslogic.service.RejectionMessageService;

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

	private static final Logger logger = LoggerFactory.getLogger(ReceiverFileRecreatorProcessor.class);

	@Autowired
	private WebSocketServerConfigurationB webSocketServerConfiguration;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		//  Receive and recreate Multipart message
		FileRecreatorBeanServer fileRecreatorBean = webSocketServerConfiguration.fileRecreatorBeanWebSocket();
		this.initializeServer(fileRecreatorBean);
		Thread fileRecreatorBeanThread = new Thread(fileRecreatorBean, "FileRecreator_"+this.getClass().getSimpleName());
		fileRecreatorBeanThread.start();
		String recreatedMultipartMessage = webSocketServerConfiguration.recreatedMultipartMessageBeanWebSocket().remove();
		logger.debug("Received message over WSS");
		
		exchange.getMessage().setBody(recreatedMultipartMessage, String.class);
	}

	private void initializeServer(FileRecreatorBeanServer fileRecreatorBean) {
		try {
			fileRecreatorBean.setup();
		} catch(Exception e) {
			logger.error("Cannot initiallize IDSCP Server", e);
			rejectionMessageService.sendRejectionMessage(null, RejectionReason.TEMPORARILY_NOT_AVAILABLE);
		}
	}

}
