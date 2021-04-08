package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.common.ContractAgreementProcessor;
import it.eng.idsa.businesslogic.processor.common.GetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.common.MapMultipartToIDSCP2;
import it.eng.idsa.businesslogic.processor.common.RegisterTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.common.MapIDSCP2toMultipart;
import it.eng.idsa.businesslogic.processor.common.ValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorReceiver;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverParseReceivedConnectorRequestProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverUsageControlProcessor;
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
	ValidateTokenProcessor validateTokenProcessor;

	@Autowired
	ContractAgreementProcessor contractAgreementProcessor;
	
	@Autowired
	ReceiverMultiPartMessageProcessor multiPartMessageProcessor;

	@Autowired
	ReceiverSendDataToDataAppProcessor sendDataToDataAppProcessor;

	@Autowired
	RegisterTransactionToCHProcessor registerTransactionToCHProcessor;

	@Autowired
	ExceptionProcessorReceiver exceptionProcessorReceiver;

	@Autowired
	GetTokenFromDapsProcessor getTokenFromDapsProcessor;

	@Autowired
	ReceiverSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;

	@Autowired
	ReceiverWebSocketSendDataToDataAppProcessor sendDataToDataAppProcessorOverWS;

	@Autowired
	ReceiverUsageControlProcessor receiverUsageControlProcessor;
	
	@Autowired
	MapIDSCP2toMultipart mapIDSCP2toMultipart;

	@Autowired
	MapMultipartToIDSCP2 mapMultipartToIDSCP2;

	@Autowired
	CamelContext camelContext;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Value("${application.idscp2.isEnabled}")
	private boolean isEnabledIdscp2;

	@Value("${application.isReceiver}")
	private boolean receiver;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		logger.debug("Starting Camel Routes...receiver side");
		camelContext.getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
		camelContext.getShutdownStrategy().setTimeout(3);

		//@formatter:off
		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(exceptionProcessorReceiver)
			.process(getTokenFromDapsProcessor)
			.process(registerTransactionToCHProcessor)
			.process(sendDataToBusinessLogicProcessor);
		
		// Camel SSL - Endpoint: B communication http
		if(!isEnabledDataAppWebSocket && !isEnabledWebSocket) {
			from("jetty://https4://0.0.0.0:" + configuration.getCamelReceiverPort() + "/data")
				.routeId("data")
				.process(connectorRequestProcessor)
				.process(validateTokenProcessor)
				.process(contractAgreementProcessor)
                .process(registerTransactionToCHProcessor)
				// Send to the Endpoint: F
                .choice()
					.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
						.process(sendDataToDataAppProcessorOverWS)
					.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
						.removeHeaders("Camel*")
						.process(sendDataToDataAppProcessor)
				.end()
				.process(multiPartMessageProcessor)
				.process(getTokenFromDapsProcessor)
				.process(receiverUsageControlProcessor)
                .process(registerTransactionToCHProcessor)
				.process(sendDataToBusinessLogicProcessor)
				.removeHeaders("Camel*");
		} 
		
		if (isEnabledDataAppWebSocket && isEnabledWebSocket) {
			
			// End point B. ECC communication (Web Socket)
			from("timer://timerEndpointB?repeatCount=-1") //EndPoint B
				.routeId("WSS")
				.process(fileRecreatorProcessor)
				.process(connectorRequestProcessor)
				.process(validateTokenProcessor)
				.process(contractAgreementProcessor)
                .process(registerTransactionToCHProcessor)
				// Send to the Endpoint: F
                .choice()
					.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
						.process(sendDataToDataAppProcessorOverWS)
					.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
						.removeHeaders("Camel*")
						.process(sendDataToDataAppProcessor)
				.end()
				.process(multiPartMessageProcessor)
				.process(getTokenFromDapsProcessor)
				.process(receiverUsageControlProcessor)
                .process(registerTransactionToCHProcessor)
				.process(sendDataToBusinessLogicProcessor);
			//@formatter:on
		}
		if (isEnabledIdscp2 && receiver && !isEnabledDataAppWebSocket) {
			logger.info("Starting IDSCP v2 Server route");
			// End point B. ECC communication (dataApp-ECC communication with http
			// and communication between ECCs with IDSCP2)
			from("idscp2server://0.0.0.0:29292?sslContextParameters=#sslContext")
					.routeId("IDSCP2 - receiver - HTTP internal")
					.log("### IDSCP2 SERVER RECEIVER: Detected Message")
					.process(mapIDSCP2toMultipart)
					.process(registerTransactionToCHProcessor)
					// Send to the Endpoint: F
					.process(sendDataToDataAppProcessor)
					.process(multiPartMessageProcessor)
					.process(registerTransactionToCHProcessor)
					.process(receiverUsageControlProcessor)
					.process(mapMultipartToIDSCP2);
		}

		if (isEnabledIdscp2 && receiver && isEnabledDataAppWebSocket) {
			// End point B. ECC communication (dataApp-ECC communication with Web Socket
			// and communication between ECCs with IDSCP2)

			from("idscp2server://0.0.0.0:29292?sslContextParameters=#sslContext")
					.routeId("IDSCP2 - receiver - WSS internal")
					.log("### IDSCP2 SERVER RECEIVER: Detected Message")
					.process(mapIDSCP2toMultipart)
					.process(registerTransactionToCHProcessor)
					// Send to the Endpoint: F
					.process(sendDataToDataAppProcessorOverWS)
					.process(multiPartMessageProcessor)
					.process(receiverUsageControlProcessor)
					.process(registerTransactionToCHProcessor)
					.process(mapMultipartToIDSCP2);
		}
	}
}