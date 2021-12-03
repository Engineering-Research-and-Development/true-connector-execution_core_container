package it.eng.idsa.businesslogic.processor.common;

import java.util.Base64;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * Processor used to apply Base64 encode logic on payload
 *
 */
@Component
public class ModifyPayloadProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(ModifyPayloadProcessor.class);

	@Value("#{new Boolean('${application.encodeDecodePayload:false}')}")
	private Boolean encodeDecodePayload;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		if(encodeDecodePayload) {
			logger.info("Base64 Encoding payload");
			MultipartMessage mm = exchange.getMessage().getBody(MultipartMessage.class);
			
			MultipartMessage mmEncoded =  new MultipartMessageBuilder()
					.withHttpHeader(mm.getHttpHeaders())
					.withHeaderHeader(mm.getHeaderHeader())
					.withHeaderContent(mm.getHeaderContent())
					.withPayloadHeader(mm.getPayloadHeader())
					.withPayloadContent(Base64.getEncoder().encodeToString(mm.getPayloadContent().getBytes()))
					.withToken(mm.getToken())
					.build();
			exchange.getMessage().setBody(mmEncoded);
		}
	}

}
