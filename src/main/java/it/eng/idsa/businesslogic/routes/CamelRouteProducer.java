package it.eng.idsa.businesslogic.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.exception.ProcessorException;
import it.eng.idsa.businesslogic.processor.producer.ProducerGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedDataProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendTransactionToCHProcessor;

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
	ProducerParseReceivedDataProcessor receiveDataToDataAppProcessor;
	
	@Autowired
	ProducerGetTokenFromDapsProcessor getTokenFromDapsProcessor;
	
	@Autowired
	ProducerSendTransactionToCHProcessor sendTransactionToCHProcessor;
	
	@Autowired
	ProducerSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;
	
	@Override
	public void configure() throws Exception {
		
		onException(ProcessorException.class,RuntimeException.class)
		.log(LoggingLevel.ERROR, "ProcessorException in the route ${body}");
		
		// Camel SSL - Endpoint: A		
		from("jetty://https4://"+configuration.getCamelProducerAddress()+"/incoming-data-app/MultipartMessage")
			.process(receiveDataToDataAppProcessor)
			.process(getTokenFromDapsProcessor)
			// Send data to Endpoint B
			.process(sendDataToBusinessLogicProcessor);
//			.process(sendTransactionToCHProcessor);
	}

}
