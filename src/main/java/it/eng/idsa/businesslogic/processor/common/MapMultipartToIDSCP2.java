package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import it.eng.idsa.multipart.domain.MultipartMessage;

import org.apache.camel.Processor;

@Component
public class MapMultipartToIDSCP2 implements Processor {

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Value("${application.idscp2.isEnabled}")
	private boolean isEnabledIdscp2;

	@Value("${application.isReceiver}")
	private boolean receiver;

	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);

		exchange.getMessage().setBody(multipartMessage.getPayloadContent());
		exchange.getMessage().setHeader("idscp2-header", multipartMessage.getHeaderContentString());

		if (isEnabledIdscp2 && !receiver) {
			String host = exchange.getMessage().getHeaders().get("Forward-To").toString().split("//")[1].split(":")[0];
			exchange.setProperty("host", host);
		}

	}

}
