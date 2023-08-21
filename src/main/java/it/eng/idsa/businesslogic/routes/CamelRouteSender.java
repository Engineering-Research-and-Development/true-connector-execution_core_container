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
import it.eng.idsa.businesslogic.processor.common.CorrelationIDProcessor;
import it.eng.idsa.businesslogic.processor.common.DeModifyPayloadProcessor;
import it.eng.idsa.businesslogic.processor.common.GetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.common.MapIDSCP2toMultipart;
import it.eng.idsa.businesslogic.processor.common.MapMultipartToIDSCP2;
import it.eng.idsa.businesslogic.processor.common.ModifyPayloadProcessor;
import it.eng.idsa.businesslogic.processor.common.OriginalMessageProcessor;
import it.eng.idsa.businesslogic.processor.common.ProtocolValidationProcessor;
import it.eng.idsa.businesslogic.processor.common.RegisterTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.common.ValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorSender;
import it.eng.idsa.businesslogic.processor.sender.SenderFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorBodyFormData;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorHttpHeader;
import it.eng.idsa.businesslogic.processor.sender.SenderSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderSendResponseToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderUsageControlProcessor;
import it.eng.idsa.businesslogic.processor.sender.registration.SenderCreateDeleteMessageProcessor;
import it.eng.idsa.businesslogic.processor.sender.registration.SenderCreatePassivateMessageProcessor;
import it.eng.idsa.businesslogic.processor.sender.registration.SenderCreateQueryBrokerMessageProcessor;
import it.eng.idsa.businesslogic.processor.sender.registration.SenderCreateRegistrationMessageProcessor;
import it.eng.idsa.businesslogic.processor.sender.registration.SenderCreateUpdateMessageProcessor;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class CamelRouteSender extends RouteBuilder {

	private static final Logger logger = LoggerFactory.getLogger(CamelRouteSender.class);

	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired(required = false)
	private SenderFileRecreatorProcessor fileRecreatorProcessor;

	@Autowired
	private SenderParseReceivedDataProcessorBodyBinary parseReceivedDataProcessorBodyBinary;

	@Autowired
	private SenderParseReceivedDataProcessorBodyFormData parseReceivedDataProcessorBodyFormData;

	@Autowired
	private SenderParseReceivedDataProcessorHttpHeader parseReceivedDataProcessorHttpHeader;

	@Autowired
	private GetTokenFromDapsProcessor getTokenFromDapsProcessor;

	@Autowired
	private RegisterTransactionToCHProcessor registerTransactionToCHProcessor;

	@Autowired
	private ContractAgreementProcessor contractAgreementProcessor;

	@Autowired
	private SenderSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;

	@Autowired
	private ValidateTokenProcessor validateTokenProcessor;

	@Autowired
	private SenderSendResponseToDataAppProcessor sendResponseToDataAppProcessor;

	@Autowired
	private ExceptionProcessorSender processorException;

	@Autowired
	private SenderUsageControlProcessor senderUsageControlProcessor;

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private SenderCreateRegistrationMessageProcessor createRegistratioMessageSender;
	@Autowired
	private SenderCreateUpdateMessageProcessor createUpdateMessageSender;
	@Autowired
	private SenderCreateDeleteMessageProcessor createDeleteMessageSender;
	@Autowired
	private SenderCreatePassivateMessageProcessor createPassivateMessageSender;
	@Autowired
	private SenderCreateQueryBrokerMessageProcessor createBrokerQueryMessageSender;
	@Autowired
	private MapMultipartToIDSCP2 mapMultipartToIDSCP2;
	@Autowired
	private MapIDSCP2toMultipart mapIDSCP2toMultipart;

	@Autowired
	private ModifyPayloadProcessor modifyPayloadProcessor;
	@Autowired
	private DeModifyPayloadProcessor deModifyPayloadProcessor;
	@Autowired
	private ConnectorHealthCheckProcessor connectorHealthCheckProcessor;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Value("${application.idscp2.isEnabled}")
	private boolean isEnabledIdscp2;

	@Value("${application.isReceiver}")
	private boolean receiver;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Autowired
	private ProtocolValidationProcessor protocolValidationProcessor;

	@Autowired
	private OriginalMessageProcessor originalMessageProcessor;

	@Autowired
	private CorrelationIDProcessor correlationIDProcessor;
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		logger.info("Starting Camel Routes...sender side");

		camelContext.getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
		camelContext.getShutdownStrategy().setTimeout(3);
//		camelContext.setCaseInsensitiveHeaders(false);

		interceptFrom().process(correlationIDProcessor);
		interceptFrom().process(connectorHealthCheckProcessor);

		//@formatter:off
		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(processorException);
		
		if(!isEnabledDataAppWebSocket) {
			logger.info("REST self registration configuration");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/register" + "?httpMethodRestrict=POST")
				.routeId("selfRegistration/register")
				.log(LoggingLevel.INFO, logger, "Registering connector to a Broker")
				.process(createRegistratioMessageSender)
				.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/update" + "?httpMethodRestrict=POST")
				.routeId("selfRegistration/update")
				.log(LoggingLevel.INFO, logger,"Updating registered connector")
				.process(createUpdateMessageSender)
				.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/delete" + "?httpMethodRestrict=POST")
				.routeId("selfRegistration/delete")
				.log(LoggingLevel.INFO, logger,"Removing registered connector from Broker")
				.process(createDeleteMessageSender)
				.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/passivate" + "?httpMethodRestrict=POST")
				.routeId("selfRegistration/passivate")
				.log(LoggingLevel.INFO, logger,"Passivating registered connector")
				.process(createPassivateMessageSender)
				.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/query" + "?httpMethodRestrict=POST")
				.routeId("selfRegistration/query")
				.log(LoggingLevel.INFO, logger,"Sending query message to Broker")
				.process(createBrokerQueryMessageSender)
				.to("direct:registrationProcess");
			
			from("direct:registrationProcess")
				.routeId("registrationProcess")
				.process(originalMessageProcessor)
				.process(protocolValidationProcessor)
				.process(getTokenFromDapsProcessor)
				.process(sendDataToBusinessLogicProcessor)
				.process(validateTokenProcessor)
				.process(sendResponseToDataAppProcessor);
		}
		
		if(!isEnabledDataAppWebSocket && !isEnabledIdscp2) {
			logger.info("REST configuration");

			// Camel SSL - Endpoint: A - Body binary
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyBinary" + "?httpMethodRestrict=POST")
				.routeId("multipartMessageBodyBinary")
				.process(parseReceivedDataProcessorBodyBinary)
				.to("direct:HTTP");
					
		    // Camel SSL - Endpoint: A - Body form-data
		    from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyFormData" + "?httpMethodRestrict=POST")
		    	.routeId("multipartMessageBodyFormData")
				.process(parseReceivedDataProcessorBodyFormData)
				.to("direct:HTTP");
		            
		    // Camel SSL - Endpoint: A - Http-header
		    from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageHttpHeader" + "?httpMethodRestrict=POST")
		    	.routeId("multipartMessageHttpHeader")
		    	.process(parseReceivedDataProcessorHttpHeader)
		    	.to("direct:HTTP");

			from("direct:HTTP")
				.routeId("HTTP")
				.process(originalMessageProcessor)
				.process(protocolValidationProcessor)
				.process(modifyPayloadProcessor)
		        .process(getTokenFromDapsProcessor)
		        .process(registerTransactionToCHProcessor)
		        // Send data to Endpoint B
		        .process(sendDataToBusinessLogicProcessor)
		        .process(deModifyPayloadProcessor)
		        .process(validateTokenProcessor)
		        .process(contractAgreementProcessor)
		        .process(senderUsageControlProcessor)
		        .process(registerTransactionToCHProcessor)
		        .process(modifyPayloadProcessor)
		        .process(sendResponseToDataAppProcessor)
				.removeHeaders("Camel*");
            } 
        
		if(isEnabledIdscp2 && !receiver && !isEnabledDataAppWebSocket) {		
			logger.info("IDSCP configuration REST dataApp");

			// End point B. ECC communication (dataApp-ECC communication with http
			// and communication between ECCs with IDSCP2)
			
			//IDSCP2 flow triggered by multipartMessageBodyBinary
			// Camel SSL - Endpoint: A - Body binary
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyBinary" + "?httpMethodRestrict=POST")
				.routeId("IDSCP2 - multipartMessageBodyBinary")
				.process(parseReceivedDataProcessorBodyBinary)
				.to("direct:IDSCP2");
			
			//IDSCP2 flow triggered by multipartMessageBodyFormData
			// Camel SSL - Endpoint: A - Body binary
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyFormData" + "?httpMethodRestrict=POST")
				.routeId("IDSCP2 - multipartMessageBodyFormData")
				.process(parseReceivedDataProcessorBodyFormData)
				.to("direct:IDSCP2");
			
			//IDSCP2 flow triggered by multipartMessageHttpHeader
			// Camel SSL - Endpoint: A - Body binary
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageHttpHeader" + "?httpMethodRestrict=POST")
				.routeId("IDSCP2 - multipartMessageHttpHeader")
            	.process(parseReceivedDataProcessorHttpHeader)
            	.to("direct:IDSCP2");
    	
			logger.info("Starting IDSCP v2 client sender route");
			
            from("direct:IDSCP2")
            	.routeId("IDSCP2 - sender - HTTP internal")
            	.log(LoggingLevel.INFO, logger,"##### STARTING IDSCP2 ARTIFACT-GIVEN MESSAGE FLOW #####")
            	.process(originalMessageProcessor)
            	.process(protocolValidationProcessor)
            	.process(registerTransactionToCHProcessor)
            	.process(mapMultipartToIDSCP2)
            	.toD("idscp2client://${exchangeProperty.host}:29292?awaitResponse=true&transportSslContextParameters=#sslContext&dapsSslContextParameters=#sslContext")
        		.log(LoggingLevel.INFO, logger,"### CLIENT RECEIVER: Detected Message")
        		.process(mapIDSCP2toMultipart)
                .process(contractAgreementProcessor)
                .process(senderUsageControlProcessor)
                .process(registerTransactionToCHProcessor)
                .process(sendResponseToDataAppProcessor)
                .removeHeader("idscp2-header");
			}
		
		if(isEnabledIdscp2 && !receiver && isEnabledDataAppWebSocket && !isEnabledWebSocket) {
			logger.info("IDSCP configuration wss dataApp");

        	// End point B. ECC communication (dataApp-ECC communication with WebSocket
    		// and communication between ECCs with IDSCP2)
	        	               	
    		logger.info("Starting IDSCP v2 client sender route");
    		logger.info("WSS communication between ECC and dataApp");
    			    
    	    from("timer://timerEndpointA?repeatCount=-1")
    	    	.routeId("Sender - dataApp-ECC over WSS and ECC-ECC over IDSCP2")
	        	.log(LoggingLevel.INFO, logger,"##### STARTING IDSCP2 ARTIFACT-GIVEN MESSAGE FLOW #####")
	            .process(fileRecreatorProcessor)
	            .process(originalMessageProcessor)
				.process(protocolValidationProcessor)
	            .process(registerTransactionToCHProcessor)		                
	            .process(mapMultipartToIDSCP2)
	            .toD("idscp2client://${exchangeProperty.host}:29292?awaitResponse=true&sslContextParameters=#sslContext")
	            .log(LoggingLevel.INFO, logger,"### CLIENT RECEIVER: Detected Message")
	        	.process(mapIDSCP2toMultipart)
		        .process(contractAgreementProcessor)
		        .process(senderUsageControlProcessor)
		        .process(registerTransactionToCHProcessor)
				.process(sendResponseToDataAppProcessor);	                	
    	    }
		
		if(!isEnabledIdscp2 && !receiver && isEnabledDataAppWebSocket) {
			logger.info("WSS configuration");

			// End point A. Communication between Data App and ECC Sender.
			//fixedRate=true&period=10s
			from("timer://timerEndpointA?repeatCount=-1") //EndPoint A
				.routeId("WSS EndPoint A")
				.process(fileRecreatorProcessor)
				.process(originalMessageProcessor)
				.process(protocolValidationProcessor)
				.process(getTokenFromDapsProcessor)
                .process(registerTransactionToCHProcessor)
				// Send data to Endpoint B
				.process(sendDataToBusinessLogicProcessor)
				.process(validateTokenProcessor)
                .process(contractAgreementProcessor)
                .process(senderUsageControlProcessor)
                .process(registerTransactionToCHProcessor)
				.process(sendResponseToDataAppProcessor);
		}
	}
}