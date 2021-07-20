package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.common.ContractAgreementProcessor;
import it.eng.idsa.businesslogic.processor.common.GetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.common.MapIDSCP2toMultipart;
import it.eng.idsa.businesslogic.processor.common.MapMultipartToIDSCP2;
import it.eng.idsa.businesslogic.processor.common.RegisterTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.common.ValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorReceiver;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorSender;
import it.eng.idsa.businesslogic.processor.sender.SenderFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataFromDAppProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorBodyFormData;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorHttpHeader;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedResponseMessage;
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
	SenderFileRecreatorProcessor fileRecreatorProcessor;

	@Autowired
	SenderParseReceivedDataProcessorBodyBinary parseReceivedDataProcessorBodyBinary;

	@Autowired
	SenderParseReceivedDataProcessorBodyFormData parseReceivedDataProcessorBodyFormData;

	@Autowired
	SenderParseReceivedDataProcessorHttpHeader parseReceivedDataProcessorHttpHeader;

	@Autowired
	GetTokenFromDapsProcessor getTokenFromDapsProcessor;

	@Autowired
	RegisterTransactionToCHProcessor registerTransactionToCHProcessor;
	
	@Autowired
	ContractAgreementProcessor contractAgreementProcessor;

	@Autowired
	SenderSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;

	@Autowired
	SenderParseReceivedResponseMessage parseReceivedResponseMessage;

	@Autowired
	ValidateTokenProcessor validateTokenProcessor;

	@Autowired
	SenderSendResponseToDataAppProcessor sendResponseToDataAppProcessor;

	@Autowired
	ExceptionProcessorSender processorException;

	@Autowired
	SenderParseReceivedDataFromDAppProcessorBodyBinary parseReceivedDataFromDAppProcessorBodyBinary;

	@Autowired
	ExceptionProcessorReceiver exceptionProcessorReceiver;

	@Autowired
	SenderUsageControlProcessor senderUsageControlProcessor;

	@Autowired
	CamelContext camelContext;

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

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Value("${application.idscp2.isEnabled}")
	private boolean isEnabledIdscp2;

	@Value("${application.isReceiver}")
	private boolean receiver;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		logger.debug("Starting Camel Routes...sender side");

		camelContext.getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
		camelContext.getShutdownStrategy().setTimeout(3);
		//@formatter:off
		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(processorException);

		if(!isEnabledDataAppWebSocket) {
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/register")
				.routeId("selfRegistration/register")
				.process(createRegistratioMessageSender)
				.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/update")
				.routeId("selfRegistration/update")
				.process(createUpdateMessageSender)
				.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/delete")
				.routeId("selfRegistration/delete")
				.process(createDeleteMessageSender)
				.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/passivate")
				.routeId("selfRegistration/passivate")
				.process(createPassivateMessageSender)
				.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/query")
				.routeId("selfRegistration/query")
				.process(createBrokerQueryMessageSender)
				.to("direct:registrationProcess");
			
			from("direct:registrationProcess")
				.routeId("registrationProcess")
				.process(getTokenFromDapsProcessor)
				.process(sendDataToBusinessLogicProcessor)
				.process(parseReceivedResponseMessage)
				.process(validateTokenProcessor)
				.process(sendResponseToDataAppProcessor);
		}
		
		if(!isEnabledDataAppWebSocket && !isEnabledIdscp2) {
			// Camel SSL - Endpoint: A - Body binary
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyBinary")
				.routeId("multipartMessageBodyBinary")
				.process(parseReceivedDataProcessorBodyBinary)
				.to("direct:HTTP");
					
		    // Camel SSL - Endpoint: A - Body form-data
		    from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyFormData")
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
		        .process(contractAgreementProcessor)
		        .process(getTokenFromDapsProcessor)
		        .process(registerTransactionToCHProcessor)
		        // Send data to Endpoint B
		        .process(sendDataToBusinessLogicProcessor)
		        .process(parseReceivedResponseMessage)
		        .process(validateTokenProcessor)
		        .process(registerTransactionToCHProcessor)
		        .process(senderUsageControlProcessor)
		        .process(sendResponseToDataAppProcessor)
				.removeHeaders("Camel*");
            } 
        
		if(isEnabledIdscp2 && !receiver && !isEnabledDataAppWebSocket) {			
			// End point B. ECC communication (dataApp-ECC communication with http
			// and communication between ECCs with IDSCP2)
			
			//IDSCP2 flow triggered by multipartMessageBodyBinary
			// Camel SSL - Endpoint: A - Body binary
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyBinary")
				.routeId("IDSCP2 - multipartMessageBodyBinary")
				.process(parseReceivedDataProcessorBodyBinary)
				.to("direct:IDSCP2");
			
			//IDSCP2 flow triggered by multipartMessageBodyFormData
			// Camel SSL - Endpoint: A - Body binary
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyFormData")
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
            	.log("##### STARTING IDSCP2 ARTIFACT-GIVEN MESSAGE FLOW #####")
            	.process(contractAgreementProcessor)
            	.process(registerTransactionToCHProcessor)
            	.process(mapMultipartToIDSCP2)
            	.toD("idscp2client://${exchangeProperty.host}:29292?awaitResponse=true&sslContextParameters=#sslContext")
        		.log("### CLIENT RECEIVER: Detected Message")
        		.process(mapIDSCP2toMultipart)
                .process(registerTransactionToCHProcessor)
                .process(senderUsageControlProcessor)
                .process(sendResponseToDataAppProcessor)
                .removeHeader("idscp2-header");
			}
		
		if(isEnabledIdscp2 && !receiver && isEnabledDataAppWebSocket && !isEnabledWebSocket) {
        	// End point B. ECC communication (dataApp-ECC communication with WebSocket
    		// and communication between ECCs with IDSCP2)
	        	               	
    		logger.info("Starting IDSCP v2 client sender route");
    		logger.info("WSS communication between ECC and dataApp");
    			    
    	    from("timer://timerEndpointA?repeatCount=-1")
    	    	.routeId("Sender - dataApp-ECC over WSS and ECC-ECC over IDSCP2")
	        	.log("##### STARTING IDSCP2 ARTIFACT-GIVEN MESSAGE FLOW #####")
	            .process(fileRecreatorProcessor)
				.process(parseReceivedDataFromDAppProcessorBodyBinary)
				.process(contractAgreementProcessor)
	            .process(registerTransactionToCHProcessor)		                
	            .process(mapMultipartToIDSCP2)
	            .toD("idscp2client://${exchangeProperty.host}:29292?awaitResponse=true&sslContextParameters=#sslContext")
	            .log("### CLIENT RECEIVER: Detected Message")
	        	.process(mapIDSCP2toMultipart)
		        .process(registerTransactionToCHProcessor)
		        .process(senderUsageControlProcessor)
				.process(sendResponseToDataAppProcessor);	                	
    	    }
		
		if(!isEnabledIdscp2 && !receiver && isEnabledDataAppWebSocket) {
			// End point A. Communication between Data App and ECC Sender.
			//fixedRate=true&period=10s
			from("timer://timerEndpointA?repeatCount=-1") //EndPoint A
				.routeId("WSS EndPoint A")
				.process(fileRecreatorProcessor)
				.process(parseReceivedDataFromDAppProcessorBodyBinary)
				.process(contractAgreementProcessor)
				.process(getTokenFromDapsProcessor)
                .process(registerTransactionToCHProcessor)
				// Send data to Endpoint B
				.process(sendDataToBusinessLogicProcessor)
				.process(parseReceivedResponseMessage)
				.process(validateTokenProcessor)
                .process(registerTransactionToCHProcessor)
                .process(senderUsageControlProcessor)
				.process(sendResponseToDataAppProcessor);
		}
	}
}