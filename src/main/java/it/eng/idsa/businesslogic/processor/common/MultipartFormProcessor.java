package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class MultipartFormProcessor implements Processor {
	
	@Autowired
	private MultipartMessageService service;

	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		
		HttpEntity resultEntity = service.createMultipartMessage(multipartMessage.getHeaderContentString(), 
				multipartMessage.getPayloadContent(),
				null, 
				ContentType.MULTIPART_FORM_DATA);
		
		exchange.setProperty("dataAppURL", "https://localhost:8083/data?bridgeEndpoint=true&sslContextParameters=#camelXMLSSLContext");
		exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, resultEntity.getContentType().getValue());
		exchange.getMessage().setBody(resultEntity.getContent());

	}

}
