package it.eng.idsa.businesslogic.processor.sender;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;


@Component
public class SenderMapIDSCP2toMultipart implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {

		
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
											.withHeaderContent((Message) exchange.getMessage().getHeader("idscp2-header"))
											.withPayloadContent(exchange.getMessage().getBody(String.class))
											.build();
			
		exchange.getMessage().setBody(multipartMessage);
		
		
	}

	

}
