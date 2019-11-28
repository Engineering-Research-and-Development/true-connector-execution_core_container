package it.eng.idsa.businesslogic.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.exception.ProcessorException;
import it.eng.idsa.businesslogic.processor.producer.GetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.producer.ParseReceivedDataProcessor;
import it.eng.idsa.businesslogic.processor.producer.SenDataToDestinationProcessor;
import it.eng.idsa.businesslogic.processor.producer.SendTransactionToCHProcessorProducer;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class CamelRouteProducer extends RouteBuilder {

	private static final Logger logger = LogManager.getLogger(CamelRouteProducer.class);
	
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	ParseReceivedDataProcessor receiveDataToDataAppProcessor;
	
	@Autowired
	GetTokenFromDapsProcessor getTokenFromDapsProcessor;
	
	@Autowired
	SenDataToDestinationProcessor senDataToDestinationProcessor;
	
	@Autowired
	SendTransactionToCHProcessorProducer sendTransactionToCHProcessor;
	
	@Override
	public void configure() throws Exception {
		
		onException(ProcessorException.class,RuntimeException.class)
		.log(LoggingLevel.ERROR, "ProcessorException in the route ${body}");
		
		// Camel SSL - Endpoint: B		
		from("jetty://https4://"+configuration.getCamelProducerAddress()+"/incoming-data-app/MultipartMessage")
			.process(receiveDataToDataAppProcessor)
			.process(getTokenFromDapsProcessor)
			.process(senDataToDestinationProcessor)
			.process(sendTransactionToCHProcessor);
		
	}

}
