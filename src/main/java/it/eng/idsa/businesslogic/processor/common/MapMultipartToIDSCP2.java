package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import it.eng.idsa.multipart.domain.MultipartMessage;

import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MapMultipartToIDSCP2 implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(MapMultipartToIDSCP2.class);

	@Value("${application.idscp2.isEnabled}")
	private Boolean isEnabledIdscp2;

	@Value("${application.isReceiver}")
	private Boolean receiver;

	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);

		exchange.getMessage().setBody(multipartMessage.getPayloadContent());
		exchange.getMessage().setHeader("idscp2-header", multipartMessage.getHeaderContentString());

		if (isEnabledIdscp2 && !receiver) {
			String host = exchange.getMessage().getHeaders().get("Forward-To").toString().split("//")[1].split(":")[0];
			exchange.setProperty("host", host);
			logger.info("IDSCP2: Message sent to idscp server: {}", host);
		}

		if (receiver)
			logger.info("IDSCP2: Message sent to idscp client");

	}

}
