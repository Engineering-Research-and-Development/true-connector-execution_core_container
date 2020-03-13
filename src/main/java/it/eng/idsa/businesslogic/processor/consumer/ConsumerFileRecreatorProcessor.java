package it.eng.idsa.businesslogic.processor.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.WebSocketConfiguration;
import it.eng.idsa.businesslogic.processor.consumer.websocket.server.FileRecreatorBean;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerFileRecreatorProcessor implements Processor {
	
	@Autowired
	private WebSocketConfiguration webSocketConfiguration;

	@Override
	public void process(Exchange exchange) throws Exception {
		FileRecreatorBean fileRecreatorBean = webSocketConfiguration.fileRecreatorBeanWebSocket();
		fileRecreatorBean.setup();
		fileRecreatorBean.start();
	}

}
