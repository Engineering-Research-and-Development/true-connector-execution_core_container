package it.eng.idsa.businesslogic.processor.sender;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketMessagingLogicA;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Antonio Scatoloni
 *
 */

@Component
public class SenderParseReceivedDataFromDAppProcessorBodyBinary implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderParseReceivedDataFromDAppProcessorBodyBinary.class);
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;

	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		String contentType;
		String forwardTo;
		String header = null;
		Message message = null;
		Map<String, Object> headesParts = new HashMap<>();
		Map<String, Object> multipartMessageParts = new HashMap<>();

		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getMessage().getHeaders();
		try {
			// Create headers parts
			// Put in the header value of the application.property: application.isEnabledDapsInteraction
			headesParts.put("Is-Enabled-Daps-Interaction", isEnabledDapsInteraction);
			contentType = null != receivedDataHeader.get("Content-Type")? receivedDataHeader.get("Content-Type").toString() : null;
			headesParts.put("Content-Type", contentType);

			//String wsURI = "wss://0.0.0.0:8086"+ HttpWebSocketServerBean.WS_URL;
			String url = HttpWebSocketMessagingLogicA.getInstance().getForwardTo();
			forwardTo = null != receivedDataHeader.get("Forward-To")? receivedDataHeader.get("Forward-To").toString() : url;
			headesParts.put("Forward-To", forwardTo);

			// Create multipart message parts
			header = receivedDataHeader.get("header").toString();
			multipartMessageParts.put("header", header);
			if(receivedDataHeader.get("payload") != null) {
				multipartMessageParts.put("payload", receivedDataHeader.get("payload").toString());
			}
			message = multipartMessageService.getMessage(multipartMessageParts.get("header"));
			
			// Return exchange
			exchange.getMessage().setHeaders(headesParts);
			exchange.getMessage().setBody(exchange.getIn().getBody());

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES,
					message);
		}

	}

}
