package it.eng.idsa.businesslogic.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.exception.ProcessorException;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendDataToDestinationProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerValidateTokenProcessor;

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
	ConsumerMultiPartMessageProcessor multiPartMessageProcessor;
	
	@Autowired
	ConsumerSendDataToDataAppProcessor sendDataToDataAppProcessor;
	
	@Autowired
	ConsumerSendDataToDestinationProcessor sendDataToDestinationProcessor;
	
	@Autowired
	ConsumerSendTransactionToCHProcessor sendTransactionToCHProcessor;
	
	@Override
	public void configure() throws Exception {

		onException(ProcessorException.class,RuntimeException.class)
			.log(LoggingLevel.ERROR, "ProcessorException in the route ${body}");

		// Camel SSL - Endpoint: B		
		from("jetty://https4://"+configuration.getCamelConsumerAddress()+"/incoming-data-channel/receivedMessage")
			.process(multiPartMessageProcessor)
			.process(validateTokenProcessor)
			// HTTP - Send data to the destination D (in the queue:outcoming)
			.process(sendDataToDestinationProcessor)
			.process(sendTransactionToCHProcessor);
		
		// Read from the ActiveMQ (from the queue:outcoming)
		from("activemq:queue:outcoming")
			.process(multiPartMessageProcessor)
			// HTTP - Send data to the endpoint F
			.process(sendDataToDataAppProcessor);
		
	}
}
