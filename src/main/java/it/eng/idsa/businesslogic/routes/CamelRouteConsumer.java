package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerExceptionMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerReceiveFromActiveMQ;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendToActiveMQ;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorConsumer;

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
	ExceptionProcessorConsumer exceptionProcessorConsumer;
	
	@Autowired
	ConsumerGetTokenFromDapsProcessor getTokenFromDapsProcessor;
	
	@Autowired
	ConsumerSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;
	
	@Autowired
	ConsumerExceptionMultiPartMessageProcessor exceptionMultiPartMessageProcessor;
	
	@Autowired
	ConsumerFileRecreatorProcessor fileRecreatorProcessor;
	
    @Autowired
    CamelContext camelContext;
	
	@Override
	public void configure() throws Exception {
		
        camelContext.getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
        camelContext.getShutdownStrategy().setTimeout(3);

		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(exceptionProcessorConsumer)
			.process(exceptionMultiPartMessageProcessor)
			.choice()
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
					.process(getTokenFromDapsProcessor)
					.process(sendDataToBusinessLogicProcessor)
					.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
						.process(sendTransactionToCHProcessor)
					.endChoice()
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
					.process(sendDataToBusinessLogicProcessor)
					.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
						.process(sendTransactionToCHProcessor)
					.endChoice()
			.endChoice();

		// Camel SSL - Endpoint: B		
		from("jetty://https4://0.0.0.0:"+configuration.getCamelConsumerPort()+"/incoming-data-channel/receivedMessage")
			.process(multiPartMessageProcessor)
			.choice()
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
					.process(validateTokenProcessor)
//					.process(sendToActiveMQ)
//					.process(receiveFromActiveMQ)
					// Send to the Endpoint: F
					.process(sendDataToDataAppProcessor)
					.process(multiPartMessageProcessor)
					.process(getTokenFromDapsProcessor)
					.process(sendDataToBusinessLogicProcessor)
					.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
							.process(sendTransactionToCHProcessor)
					.endChoice()
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
					// Send to the Endpoint: F
					.process(sendDataToDataAppProcessor)
					.process(multiPartMessageProcessor)
					.process(sendDataToBusinessLogicProcessor)
					.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
							.process(sendTransactionToCHProcessor)
					.endChoice()
			.endChoice();
		
		// TODO: Improve this initialization
		// Camel WebSocket - Endpoint B
		boolean startupRoute = true;
		from("timer://simpleTimer?repeatCount=-1")
			.process(fileRecreatorProcessor)
			.process(multiPartMessageProcessor)
			.choice()
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
					.process(validateTokenProcessor)
//					.process(sendToActiveMQ)
//					.process(receiveFromActiveMQ)
					// Send to the Endpoint: F
					.process(sendDataToDataAppProcessor)
					.process(multiPartMessageProcessor)
					.process(getTokenFromDapsProcessor)
					.process(sendDataToBusinessLogicProcessor)
					.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
							.process(sendTransactionToCHProcessor)
					.endChoice()
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
					// Send to the Endpoint: F
					.process(sendDataToDataAppProcessor)
					.process(multiPartMessageProcessor)
					.process(sendDataToBusinessLogicProcessor)
					.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
							.process(sendTransactionToCHProcessor)
					.endChoice()
			.endChoice();			
	}
}
