package it.eng.idsa.businesslogic.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerReceiveFromActiveMQ;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendToActiveMQ;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class CamelRouteConsumer extends RouteBuilder {
	
	private static final Logger logger = LogManager.getLogger(CamelRouteConsumer.class);
	
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	ConsumerValidateTokenProcessor validateTokenProcessor;
	
	@Autowired
	ConsumerSendToActiveMQ sendToActiveMQ;
	
	@Autowired
	ConsumerReceiveFromActiveMQ receiveFromActiveMQ;
	
	@Autowired
	ConsumerMultiPartMessageProcessor multiPartMessageProcessor;
	
	@Autowired
	ConsumerSendDataToDataAppProcessor sendDataToDataAppProcessor;
	
	@Autowired
	ConsumerSendTransactionToCHProcessor sendTransactionToCHProcessor;
	
	@Autowired
	ExceptionProcessor processorException;
	
	@Autowired
	ConsumerGetTokenFromDapsProcessor getTokenFromDapsProcessor;
	
	@Autowired
	ConsumerSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;
	
	@Override
	public void configure() throws Exception {

		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(processorException);

		// Camel SSL - Endpoint: B		
		from("jetty://https4://0.0.0.0:"+configuration.getCamelConsumerPort()+"/incoming-data-channel/receivedMessage")
			.process(multiPartMessageProcessor)
			.process(validateTokenProcessor)
//			.process(sendToActiveMQ)
//			.process(receiveFromActiveMQ)
			// Send to the Endpoint: F
			.process(sendDataToDataAppProcessor)
			.process(multiPartMessageProcessor)
			.process(getTokenFromDapsProcessor)
			.process(sendDataToBusinessLogicProcessor);
//			.process(sendTransactionToCHProcessor);
		
	}
}
