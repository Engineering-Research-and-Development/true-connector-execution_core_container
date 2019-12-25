package it.eng.idsa.businesslogic.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedDataProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedDataProcessorBodyFormData;
import it.eng.idsa.businesslogic.processor.producer.ProducerReceiveFromActiveMQ;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendToActiveMQ;
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
	ProducerParseReceivedDataProcessorBodyBinary parseReceivedDataProcessorBodyBinary;
	
	@Autowired
	ProducerParseReceivedDataProcessorBodyFormData parseReceivedDataProcessorBodyFormData;

	@Autowired
	ProducerGetTokenFromDapsProcessor getTokenFromDapsProcessor;
	
	@Autowired
	ProducerSendToActiveMQ sendToActiveMQ;
	
	@Autowired
	ProducerReceiveFromActiveMQ receiveFromActiveMQ;

	@Autowired
	ProducerSendTransactionToCHProcessor sendTransactionToCHProcessor;

	@Autowired
	ProducerSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;

	@Autowired
	ExceptionProcessor processorException;

	@Override
	public void configure() throws Exception {

		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(processorException);

		// Camel SSL - Endpoint: A - Body binary
		from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/incoming-data-app/multipartMessageBodyBinary")
				.process(parseReceivedDataProcessorBodyBinary)
				.process(getTokenFromDapsProcessor)
				.process(sendToActiveMQ)
				.process(receiveFromActiveMQ)
				// Send data to Endpoint B
				.process(sendDataToBusinessLogicProcessor);
//				.process(sendTransactionToCHProcessor);
				
		// Camel SSL - Endpoint: A - Body form-data
		from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/incoming-data-app/multipartMessageBodyFormData")
				.process(parseReceivedDataProcessorBodyFormData)
				.process(getTokenFromDapsProcessor)
				.process(sendToActiveMQ)
				.process(receiveFromActiveMQ)
				// Send data to Endpoint B
				.process(sendDataToBusinessLogicProcessor);
//				.process(sendTransactionToCHProcessor);
	}

}
