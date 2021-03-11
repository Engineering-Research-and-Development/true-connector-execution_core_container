package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fhg.aisec.ids.camel.idscp2.processors.IdsMessageTypeExtractionProcessor;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.common.GetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.common.MapMultipartToIDSCP2;
import it.eng.idsa.businesslogic.processor.common.RegisterTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.common.ValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorReceiver;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverParseReceivedConnectorRequestProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverStaticResponseMessageProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverUsageControlProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverWebSocketSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderMapIDSCP2toMultipart;

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
	IdsMessageTypeExtractionProcessor IdsMessageTypeExtractionProcessor;

	@Autowired
	ReceiverStaticResponseMessageProcessor receiverStaticResponseMessageProcessor;

	@Autowired
	SenderMapIDSCP2toMultipart senderMapIDSCP2toMultipart;

	@Autowired
	MapMultipartToIDSCP2 mapMultipartToIDSCP2;

	@Autowired
	CamelContext camelContext;

	@Value("${application.idscp.isEnabled}")
	private boolean isEnabledIdscp;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Value("${application.idscp2.isEnabled}")
	private boolean isEnabledIdscp2;

	@Value("${application.isReceiver}")
	private boolean receiver;

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

		// Camel SSL - Endpoint: B
		if(!isEnabledIdscp && !isEnabledWebSocket) {
			from("jetty://https4://0.0.0.0:" + configuration.getCamelReceiverPort() + "/data")
				.process(connectorRequestProcessor)
				.process(validateTokenProcessor)
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
		} else if (isEnabledIdscp || isEnabledWebSocket) {
			// End point B. ECC communication (Web Socket or IDSCP)
			from("timer://timerEndpointB?repeatCount=-1") //EndPoint B
				.process(fileRecreatorProcessor)
				.process(connectorRequestProcessor)
				.process(validateTokenProcessor)
                .process(registerTransactionToCHProcessor)
				// Send to the Endpoint: F
				.choice()
					.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
						.process(sendDataToDataAppProcessorOverWS)
					.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
						.process(sendDataToDataAppProcessor)
				.end()
				.process(multiPartMessageProcessor)
				.process(getTokenFromDapsProcessor)
				.process(receiverUsageControlProcessor)
                .process(registerTransactionToCHProcessor)
				.process(sendDataToBusinessLogicProcessor);
			//@formatter:on
		}

		if (isEnabledIdscp2 && receiver) {
			logger.info("Starting IDSCP v2 Server route");

			from("idscp2server://0.0.0.0:29292?sslContextParameters=#sslContext&useIdsMessages=true")
					.process(IdsMessageTypeExtractionProcessor)
					.choice()
						.when()
	
						.simple("${exchangeProperty.ids-type} == 'ArtifactRequestMessage'")
						.log("### IDSCP2 SERVER RECEIVER: Detected Message type: ${exchangeProperty.ids-type}")
						// .log("###LOG :\n${body}\n### Header: ###\n${headers[idscp2-header]}")
						.log("### Handle ArtifactRequestMessage ###")
	
						.process(senderMapIDSCP2toMultipart)
	
						.process(registerTransactionToCHProcessor)
						// Send to the Endpoint: F
						.process(sendDataToDataAppProcessor)
						.process(multiPartMessageProcessor)
						.process(registerTransactionToCHProcessor)
						.process(receiverUsageControlProcessor)
	
						.process(mapMultipartToIDSCP2)
						.delay().constant(5000)
					.endChoice()
					.otherwise()
						.log("### IDSCP2 SERVER RECEIVER: Detected Message type: ${exchangeProperty.ids-type}")
						.log("### Server received (otherwise branch):\n${body}\n### Header: ###\n${headers[idscp2-header]}")
						.removeHeader("idscp2-header").setBody().simple("${null}");

		}

	}
}