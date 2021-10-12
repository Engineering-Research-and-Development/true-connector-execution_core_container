package it.eng.idsa.businesslogic.processor.sender.registration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

public abstract class AbstractCreateRegistrationMessage implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractCreateRegistrationMessage.class);
	
	@Autowired
	private ProtocolValidationService protocolValidationService;

	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> headersParts = new HashMap<String, Object>();
		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getMessage().getHeaders();
		
		String connector = selfDescriptionService.getConnectorSelfDescription();
		Message connectorAvailable = getConnectorMessage();

		String  registrationMessage = getObjectAsString(connectorAvailable);
		MultipartMessage multipartMessage = null;
		logger.debug("Message for sending towards Broker - header\n{}", registrationMessage);
		logger.debug("Message for sending towards Broker - payload\n{}", connector);

		if (connector != null) {
			multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(connectorAvailable)
					.withPayloadContent(connector)
					.build();
		}else {
			multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(connectorAvailable)
					.build();
		}
		
		String forwardTo = (String) receivedDataHeader.get("Forward-To");
		forwardTo = protocolValidationService.validateProtocol(forwardTo, multipartMessage.getHeaderContent());
		headersParts.put("Forward-To", forwardTo);
		headersParts.put("Payload-Content-Type", MediaType.APPLICATION_JSON.toString());
		
		// Return exchange
		exchange.getMessage().setHeaders(headersParts);
		exchange.getMessage().setBody(multipartMessage);

	}

	abstract Message getConnectorMessage();

	private String getObjectAsString(Object toSerialize) {
		final Serializer serializer = new Serializer();
		String result = null;
		try {
			result = serializer.serialize(toSerialize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
