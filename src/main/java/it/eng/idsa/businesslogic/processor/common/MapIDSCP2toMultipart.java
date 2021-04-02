package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class MapIDSCP2toMultipart implements Processor {

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {
		Message msg = null;
		
			if(exchange.getMessage().getHeader("idscp2-header") instanceof String)
			{
				msg = multipartMessageService.getMessage(exchange.getMessage().getHeader("idscp2-header"));
			}
			else {
				msg = (Message) exchange.getMessage().getHeader("idscp2-header");
			}
		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
											.withHeaderContent(msg)
											.withPayloadContent(exchange.getMessage().getBody(String.class))
											.build();

		exchange.getMessage().setBody(multipartMessage);
	}
}
