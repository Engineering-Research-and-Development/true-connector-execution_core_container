package it.eng.idsa.businesslogic.routes;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.consumer.*;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorConsumer;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

	@Autowired(required = false)
	ConsumerFileRecreatorProcessor fileRecreatorProcessor;

	@Autowired
	ConsumerValidateTokenProcessor validateTokenProcessor;
	
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
	ConsumerWebSocketSendDataToDataAppProcessor sendDataToDataAppProcessorOverWS;

	@Autowired
	CamelContext camelContext;

	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Override
	public void configure() throws Exception {
		logger.debug("Starting Camel Routes...consumer side");
        camelContext.getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
        camelContext.getShutdownStrategy().setTimeout(3);
		//@formatter:off
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
						//.process(sendTransactionToCHProcessor)
					.endChoice()
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
					.process(sendDataToBusinessLogicProcessor)
					.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
						//.process(sendTransactionToCHProcessor)
					.endChoice()
			.endChoice();

		// Camel SSL - Endpoint: B
		if(!isEnabledIdscp && !isEnabledWebSocket) {
			from("jetty://https4://0.0.0.0:" + configuration.getCamelConsumerPort() + "/incoming-data-channel/receivedMessage")
					.process(multiPartMessageProcessor)
					.choice()
					.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
						.process(validateTokenProcessor)
						//.process(sendToActiveMQ)
						//.process(receiveFromActiveMQ)
						// Send to the Endpoint: F
						.choice()
						.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
							.process(sendDataToDataAppProcessorOverWS)
						.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
							.process(sendDataToDataAppProcessor)
						.endChoice()
						.process(multiPartMessageProcessor)
						.process(getTokenFromDapsProcessor)
						.process(sendDataToBusinessLogicProcessor)
						.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
							.process(sendTransactionToCHProcessor)
						.endChoice()
					.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
						// Send to the Endpoint: F
						.choice()
						.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
							.process(sendDataToDataAppProcessorOverWS)
						.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
							.process(sendDataToDataAppProcessor)
						.endChoice()
						.process(multiPartMessageProcessor)
						.process(sendDataToBusinessLogicProcessor)
						.choice()
						.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
							//.process(sendTransactionToCHProcessor)
						.endChoice()
					.endChoice();
		} else if (isEnabledIdscp || isEnabledWebSocket) {
			// End point B. ECC communication (Web Socket or IDSCP)
			from("timer://timerEndpointB?repeatCount=-1") //EndPoint B
					.process(fileRecreatorProcessor)
					.process(multiPartMessageProcessor)
					.choice()
						.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
							.process(validateTokenProcessor)
							// Send to the Endpoint: F
							.choice()
								.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
									.process(sendDataToDataAppProcessorOverWS)
								.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
								.process(sendDataToDataAppProcessor)
							.endChoice()
								.process(multiPartMessageProcessor)
								.process(getTokenFromDapsProcessor)
								.process(sendDataToBusinessLogicProcessor)
							.choice()
								.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
									.process(sendTransactionToCHProcessor)
							.endChoice()
								.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
							// Send to the Endpoint: F
							.choice()
							.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
								.process(sendDataToDataAppProcessorOverWS)
							.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
								.process(sendDataToDataAppProcessor)
							.endChoice()
							.process(multiPartMessageProcessor)
							.process(sendDataToBusinessLogicProcessor)
							.choice()
								.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
									// .process(sendTransactionToCHProcessor)
							.endChoice()
					.endChoice();
			//@formatter:on
		}

	}
}
