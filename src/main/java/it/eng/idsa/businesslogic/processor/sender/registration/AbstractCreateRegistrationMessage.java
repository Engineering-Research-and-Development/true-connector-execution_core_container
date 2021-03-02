package it.eng.idsa.businesslogic.processor.sender.registration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

public abstract class AbstractCreateRegistrationMessage implements Processor {
	
	private static final Logger logger = LogManager.getLogger(AbstractCreateRegistrationMessage.class);

	@Autowired
	private SelfDescriptionService selfDescriptionService;

	@Override
	public void process(Exchange exchange) throws Exception {
		Map<String, Object> headersParts = new HashMap<String, Object>();
		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getMessage().getHeaders();

		headersParts.put("Forward-To", receivedDataHeader.get("Forward-To").toString());


		String connector = selfDescriptionService.getConnectorSelfDescription();
		Message connectorAvailable = getConnectorMessage();

		String  registrationMessage = geObjectAsString(connectorAvailable);
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
		// Return exchange
		exchange.getMessage().setHeaders(headersParts);
		exchange.getMessage().setBody(multipartMessage);

	}

	abstract Message getConnectorMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException;

	private String geObjectAsString(Object toSerialize) {
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
