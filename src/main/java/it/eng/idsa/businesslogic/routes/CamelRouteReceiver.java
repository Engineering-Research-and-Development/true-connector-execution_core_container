package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.common.SendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorReceiver;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverExceptionMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverParseReceivedConnectorRequestProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverUsageControlProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverWebSocketSendDataToDataAppProcessor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class CamelRouteReceiver extends RouteBuilder {
	
	private static final Logger logger = LogManager.getLogger(CamelRouteReceiver.class);
	
	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired(required = false)
	ReceiverFileRecreatorProcessor fileRecreatorProcessor;
	
	@Autowired
	ReceiverParseReceivedConnectorRequestProcessor connectorRequestProcessor;

	@Autowired
	ReceiverValidateTokenProcessor validateTokenProcessor;
	
	@Autowired
	ReceiverMultiPartMessageProcessor multiPartMessageProcessor;
	
	@Autowired
	ReceiverSendDataToDataAppProcessor sendDataToDataAppProcessor;
	
	@Autowired
	SendTransactionToCHProcessor sendTransactionToCHProcessor;
	
	@Autowired
	ExceptionProcessorReceiver exceptionProcessorReceiver;
	
	@Autowired
	ReceiverGetTokenFromDapsProcessor getTokenFromDapsProcessor;
	
	@Autowired
	ReceiverSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;
	
	@Autowired
	ReceiverExceptionMultiPartMessageProcessor exceptionMultiPartMessageProcessor;

	@Autowired
	ReceiverWebSocketSendDataToDataAppProcessor sendDataToDataAppProcessorOverWS;

	@Autowired
	ReceiverUsageControlProcessor receiverUsageControlProcessor;

	@Autowired
	CamelContext camelContext;

	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Override
	public void configure() throws Exception {
		logger.debug("Starting Camel Routes...receiver side");
        camelContext.getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
        camelContext.getShutdownStrategy().setTimeout(3);

      //@formatter:off
		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(exceptionProcessorReceiver)
			.process(exceptionMultiPartMessageProcessor)
			.choice()
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
					.process(getTokenFromDapsProcessor)
					.process(sendDataToBusinessLogicProcessor)
					.process(sendTransactionToCHProcessor)
				.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
					.process(sendDataToBusinessLogicProcessor)
					.process(sendTransactionToCHProcessor)
			.endChoice();

		// Camel SSL - Endpoint: B
		if(!isEnabledIdscp && !isEnabledWebSocket) {
			from("jetty://https4://0.0.0.0:" + configuration.getCamelReceiverPort() + "/incoming-data-channel/receivedMessage")
					.process(connectorRequestProcessor)
					.choice()
					.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
						.process(validateTokenProcessor)
	                    .process(sendTransactionToCHProcessor)
						// Send to the Endpoint: F
						.choice()
						.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
							.process(sendDataToDataAppProcessorOverWS)
						.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
							.removeHeaders("Camel*")
							.process(sendDataToDataAppProcessor)
						.endChoice()
						.process(multiPartMessageProcessor)
						.process(getTokenFromDapsProcessor)
						.process(receiverUsageControlProcessor)
	                    .process(sendTransactionToCHProcessor)
						.process(sendDataToBusinessLogicProcessor)
					.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
		                .process(sendTransactionToCHProcessor)
						// Send to the Endpoint: F
						.choice()
						.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
							.process(sendDataToDataAppProcessorOverWS)
						.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
							.removeHeaders("Camel*")
							.process(sendDataToDataAppProcessor)
						.endChoice()
						.process(multiPartMessageProcessor)
						.process(receiverUsageControlProcessor)
	                    .process(sendTransactionToCHProcessor)
						.process(sendDataToBusinessLogicProcessor)
						.removeHeaders("Camel*")
					.endChoice();
		} else if (isEnabledIdscp || isEnabledWebSocket) {
			// End point B. ECC communication (Web Socket or IDSCP)
			from("timer://timerEndpointB?repeatCount=-1") //EndPoint B
					.process(fileRecreatorProcessor)
					.process(connectorRequestProcessor)
					.choice()
						.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
							.process(validateTokenProcessor)
	                        .process(sendTransactionToCHProcessor)
							// Send to the Endpoint: F
							.choice()
								.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
									.process(sendDataToDataAppProcessorOverWS)
								.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
								.process(sendDataToDataAppProcessor)
							.endChoice()
								.process(multiPartMessageProcessor)
								.process(getTokenFromDapsProcessor)
								.process(receiverUsageControlProcessor)
								.log("Registering response to clearing house")
	                            .process(sendTransactionToCHProcessor)
								.process(sendDataToBusinessLogicProcessor)
						.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
		                    	.process(sendTransactionToCHProcessor)
							// Send to the Endpoint: F
							.choice()
							.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
								.process(sendDataToDataAppProcessorOverWS)
							.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
								.process(sendDataToDataAppProcessor)
							.endChoice()
								.process(multiPartMessageProcessor)
								.process(receiverUsageControlProcessor)
								.log("Registering response to clearing house")
	                        	.process(sendTransactionToCHProcessor)
							.process(sendDataToBusinessLogicProcessor)
					.endChoice();
			//@formatter:on
		}

	}
}