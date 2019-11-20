package it.eng.idsa.businesslogic.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.exception.ProcessorException;
import it.eng.idsa.businesslogic.processor.MultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.SendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.SendDataToDestinationProcessor;
import it.eng.idsa.businesslogic.processor.SendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.ValidateTokenProcessor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class CamelRoute extends RouteBuilder {
	
	private static final Logger logger = LogManager.getLogger(CamelRoute.class);
	
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	ValidateTokenProcessor validateTokenProcessor;
	
	@Autowired
	MultiPartMessageProcessor multiPartMessageProcessor;
	
	@Autowired
	SendDataToDataAppProcessor sendDataToDataAppProcessor;
	
	@Autowired
	SendDataToDestinationProcessor sendDataToDestinationProcessor;
	
	@Autowired
	SendTransactionToCHProcessor sendTransactionToCHProcessor;
	
	@Override
	public void configure() throws Exception {

		onException(ProcessorException.class,RuntimeException.class)
			.log(LoggingLevel.ERROR, "ProcessorException in the route ${body}");

		// Camel SSL - Endpoint: B		
		from("jetty://https4://"+configuration.getCamelConsumerAddress()+"/incoming-data-channel/receivedMessage")
			.process(multiPartMessageProcessor)
			.process(validateTokenProcessor)
			// HTTP - Send data to the destination D
			.process(sendDataToDestinationProcessor)
			.process(sendTransactionToCHProcessor);
		
		// Read from the ActiveMQ
		from("activemq:queue:outcoming")
			.process(multiPartMessageProcessor)
			// HTTP - Send data to the endpoint F
			.process(sendDataToDataAppProcessor);
		
	}
}
