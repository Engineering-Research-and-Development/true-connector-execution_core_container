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
 * Processor used to apply Base64 decode logic on payload
 *
 */
@Component
public class DeModifyPayloadProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(DeModifyPayloadProcessor.class);

	@Value("#{new Boolean('${application.encodeDecodePayload:false}')}")
	private Boolean encodeDecodePayload;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		if(encodeDecodePayload) {
			logger.info("Base64 Decoding payload");
			try {
				MultipartMessage mm = exchange.getMessage().getBody(MultipartMessage.class);
				
				MultipartMessage mmEncoded =  new MultipartMessageBuilder()
						.withHttpHeader(mm.getHttpHeaders())
						.withHeaderHeader(mm.getHeaderHeader())
						.withHeaderContent(mm.getHeaderContent())
						.withPayloadHeader(mm.getPayloadHeader())
						.withPayloadContent(new String(Base64.getDecoder().decode(mm.getPayloadContent())))
						.withToken(mm.getToken())
						.build();
				exchange.getMessage().setBody(mmEncoded);
			} catch (IllegalArgumentException ex) {
				logger.warn("---------- Payload is not valid Base64 encoded string - will not perform encoding. Continue with original payload ---------");
			}
		}
	}

}
