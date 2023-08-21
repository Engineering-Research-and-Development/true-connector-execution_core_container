package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.common.ConnectorHealthCheckProcessor;
import it.eng.idsa.businesslogic.processor.common.ContractAgreementProcessor;
import it.eng.idsa.businesslogic.processor.common.DeModifyPayloadProcessor;
import it.eng.idsa.businesslogic.processor.common.GetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.common.MapIDSCP2toMultipart;
import it.eng.idsa.businesslogic.processor.common.MapMultipartToIDSCP2;
import it.eng.idsa.businesslogic.processor.common.ModifyPayloadProcessor;
import it.eng.idsa.businesslogic.processor.common.OriginalMessageProcessor;
import it.eng.idsa.businesslogic.processor.common.RegisterTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.common.ValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorReceiver;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverParseReceivedConnectorRequestProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverUsageControlProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverVersionCheckProcessor;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverWebSocketSendDataToDataAppProcessor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class CamelRouteReceiver extends RouteBuilder {

	private static final Logger logger = LoggerFactory.getLogger(CamelRouteReceiver.class);

	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired(required = false)
	private ReceiverFileRecreatorProcessor fileRecreatorProcessor;

	@Autowired
	private ReceiverParseReceivedConnectorRequestProcessor connectorRequestProcessor;

	@Autowired
	private ValidateTokenProcessor validateTokenProcessor;

	@Autowired
	private ContractAgreementProcessor contractAgreementProcessor;
	
	@Autowired
	private ReceiverSendDataToDataAppProcessor sendDataToDataAppProcessor;

	@Autowired
	private RegisterTransactionToCHProcessor registerTransactionToCHProcessor;

	@Autowired
	private ExceptionProcessorReceiver exceptionProcessorReceiver;

	@Autowired
	private GetTokenFromDapsProcessor getTokenFromDapsProcessor;

	@Autowired
	private ReceiverSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;

	@Autowired
	private ReceiverWebSocketSendDataToDataAppProcessor sendDataToDataAppProcessorOverWS;

	@Autowired
	private ReceiverUsageControlProcessor receiverUsageControlProcessor;
	
	@Autowired
	private ReceiverVersionCheckProcessor receiverVersionCheckProcessor;
	
	@Autowired
	private MapIDSCP2toMultipart mapIDSCP2toMultipart;

	@Autowired
	private MapMultipartToIDSCP2 mapMultipartToIDSCP2;

	@Autowired
	private CamelContext camelContext;
	
	@Autowired
	private ModifyPayloadProcessor modifyPayloadProcessor;
	@Autowired
	private DeModifyPayloadProcessor deModifyPayloadProcessor;
	
	@Autowired
	private OriginalMessageProcessor originalMessageProcessor;
	
	@Autowired
	private ConnectorHealthCheckProcessor connectorHealthCheckProcessor;

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
		logger.info("Starting Camel Routes...receiver side");
		camelContext.getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
		camelContext.getShutdownStrategy().setTimeout(3);
//		camelContext.setCaseInsensitiveHeaders(false);
		
		interceptFrom().process(connectorHealthCheckProcessor);

		//@formatter:off
		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(exceptionProcessorReceiver)
			.process(sendDataToBusinessLogicProcessor);
		
		// Camel SSL - Endpoint: B communication http
		if(!isEnabledWebSocket && !isEnabledIdscp2) {
			logger.info("REST Configuration");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelReceiverPort() + "/data" + "?httpMethodRestrict=POST")
				.routeId("data")
				.process(connectorRequestProcessor)
				.process(receiverVersionCheckProcessor)
				.process(originalMessageProcessor)
				.process(deModifyPayloadProcessor)
				.process(validateTokenProcessor)
                .process(registerTransactionToCHProcessor)
                .process(modifyPayloadProcessor)
				// Send to the Endpoint: F
                .choice()
					.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
						.process(sendDataToDataAppProcessorOverWS)
					.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
						.removeHeaders("Camel*")
						.process(sendDataToDataAppProcessor)
				.end()
				.process(deModifyPayloadProcessor)
				.process(getTokenFromDapsProcessor)
				.process(contractAgreementProcessor)
				.process(receiverUsageControlProcessor)
				.process(registerTransactionToCHProcessor)
                .process(modifyPayloadProcessor)
				.process(sendDataToBusinessLogicProcessor)
				.removeHeaders("Camel*");
		} 
		
		if (isEnabledWebSocket) {
			logger.info("WSS Configuration");
			// End point B. ECC communication (Web Socket)
			from("timer://timerEndpointB?repeatCount=-1") //EndPoint B
				.routeId("WSS")
				.process(fileRecreatorProcessor)
				.process(connectorRequestProcessor)
				.process(receiverVersionCheckProcessor)
				.process(originalMessageProcessor)
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
				.process(getTokenFromDapsProcessor)
				.process(contractAgreementProcessor)
				.process(receiverUsageControlProcessor)
				.process(registerTransactionToCHProcessor)
				.process(sendDataToBusinessLogicProcessor);
			//@formatter:on
		}
		if (isEnabledIdscp2 && receiver && !isEnabledDataAppWebSocket) {
			logger.info("Starting IDSCP v2 Receiver route");
			// End point B. ECC communication (dataApp-ECC communication with http
			// and communication between ECCs with IDSCP2)
			from("idscp2server://0.0.0.0:29292?transportSslContextParameters=#sslContext&dapsSslContextParameters=#sslContext")
					.routeId("IDSCP2 - receiver - HTTP internal")
					.log(LoggingLevel.INFO, logger,"### IDSCP2 SERVER RECEIVER: Detected Message")
					.process(mapIDSCP2toMultipart)
					.process(receiverVersionCheckProcessor)
					.process(originalMessageProcessor)
					.process(registerTransactionToCHProcessor)
					// Send to the Endpoint: F
					.process(sendDataToDataAppProcessor)
					.process(contractAgreementProcessor)
					.process(receiverUsageControlProcessor)
					.process(registerTransactionToCHProcessor)
					.process(mapMultipartToIDSCP2);
		}

		if (isEnabledIdscp2 && receiver && isEnabledDataAppWebSocket) {
			// End point B. ECC communication (dataApp-ECC communication with Web Socket
			// and communication between ECCs with IDSCP2)
			logger.info("IDSCP configuration wss dataApp");

			from("idscp2server://0.0.0.0:29292?sslContextParameters=#sslContext")
					.routeId("Receiver - dataApp-ECC over WSS and ECC-ECC over IDSCP2")
					.log(LoggingLevel.INFO, logger,"### IDSCP2 SERVER RECEIVER: Detected Message")
					.process(mapIDSCP2toMultipart)
					.process(receiverVersionCheckProcessor)
					.process(originalMessageProcessor)
					.process(registerTransactionToCHProcessor)
					// Send to the Endpoint: F
					.process(sendDataToDataAppProcessorOverWS)
					.process(contractAgreementProcessor)
					.process(receiverUsageControlProcessor)
					.process(registerTransactionToCHProcessor)
					.process(mapMultipartToIDSCP2);
		}
	}
}