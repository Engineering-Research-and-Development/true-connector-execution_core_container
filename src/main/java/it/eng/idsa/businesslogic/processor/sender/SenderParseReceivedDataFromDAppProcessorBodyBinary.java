package it.eng.idsa.businesslogic.processor.sender;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketMessagingLogicA;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Antonio Scatoloni
 *
 */

@Component
public class SenderParseReceivedDataFromDAppProcessorBodyBinary implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderParseReceivedDataFromDAppProcessorBodyBinary.class);

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
			contentType = null != receivedDataHeader.get("Content-Type")? receivedDataHeader.get("Content-Type").toString() : null;
			headesParts.put("Content-Type", contentType);

			//String wsURI = "wss://0.0.0.0:8086"+ HttpWebSocketServerBean.WS_URL;
			String url = HttpWebSocketMessagingLogicA.getInstance().getForwardTo();
			forwardTo = null != receivedDataHeader.get("Forward-To")? receivedDataHeader.get("Forward-To").toString() : url;
			headesParts.put("Forward-To", forwardTo);

			// Create multipart message parts
			header = receivedDataHeader.get(MessagePart.HEADER).toString();
			multipartMessageParts.put(MessagePart.HEADER, header);
			if(null != receivedDataHeader.get(MessagePart.PAYLOAD) && StringUtils.isNotBlank(MessagePart.PAYLOAD)) {
				multipartMessageParts.put(MessagePart.PAYLOAD, receivedDataHeader.get(MessagePart.PAYLOAD).toString());
			}
			message = multipartMessageService.getMessage(multipartMessageParts.get(MessagePart.HEADER));
			
			// Return exchange
			exchange.getMessage().setHeaders(headesParts);
			exchange.getMessage().setBody(exchange.getMessage().getBody());

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES,
					message);
		}

	}

}
