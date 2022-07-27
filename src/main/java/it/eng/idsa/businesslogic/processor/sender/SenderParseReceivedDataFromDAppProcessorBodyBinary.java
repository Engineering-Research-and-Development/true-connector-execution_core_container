package it.eng.idsa.businesslogic.processor.sender;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketMessagingLogicA;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * 
 * @author Antonio Scatoloni
 *
 */

@Component
public class SenderParseReceivedDataFromDAppProcessorBodyBinary implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(SenderParseReceivedDataFromDAppProcessorBodyBinary.class);
	
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

			// Create multipart message parts
			header = receivedDataHeader.get(MessagePart.HEADER).toString();
			multipartMessageParts.put(MessagePart.HEADER, header);
			if(receivedDataHeader.get(MessagePart.PAYLOAD) != null) {
				multipartMessageParts.put(MessagePart.PAYLOAD, receivedDataHeader.get(MessagePart.PAYLOAD).toString());
			}
			message = MultipartMessageProcessor.getMessage(multipartMessageParts.get(MessagePart.HEADER));
			
			//String wsURI = "wss://0.0.0.0:8086"+ HttpWebSocketServerBean.WS_URL;
			String url = HttpWebSocketMessagingLogicA.getInstance().getForwardTo();
				forwardTo = null != receivedDataHeader.get("Forward-To") ?
						(String) receivedDataHeader.get("Forward-To") : url;
			headesParts.put("Forward-To", forwardTo);
			
			// Return exchange
			exchange.getMessage().setHeaders(headesParts);
			exchange.getMessage().setBody(exchange.getMessage().getBody());

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e.getMessage());
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES);
		}
	}

}
