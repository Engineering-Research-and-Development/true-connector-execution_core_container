package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import it.eng.idsa.multipart.domain.MultipartMessage;

import org.apache.camel.Processor;

@Component
public class MapMultipartToIDSCP2 implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);

		exchange.getMessage().setBody(multipartMessage.getPayloadContent());
		exchange.getMessage().setHeader("idscp2-header", multipartMessage.getHeaderContent());

	}

}
