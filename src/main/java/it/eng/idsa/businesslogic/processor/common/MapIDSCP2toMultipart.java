package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class MapIDSCP2toMultipart implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(MapIDSCP2toMultipart.class);
	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Value("${application.isEnabledUsageControl:false}")
    private boolean isEnabledUsageControl;

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
		
		logger.info("IDSCP2: ids message converted to multipart");

		exchange.getMessage().setBody(multipartMessage);
		if(isEnabledUsageControl) {
            exchange.getMessage().setHeader("Original-Message-Header", multipartMessage.getHeaderContentString());
        }
	}
}
