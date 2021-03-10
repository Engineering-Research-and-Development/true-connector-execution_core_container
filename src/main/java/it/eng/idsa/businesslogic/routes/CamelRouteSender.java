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
import it.eng.idsa.businesslogic.processor.common.RegisterTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.common.ValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorReceiver;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorSender;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverMapMultipartToIDSCP2;
import it.eng.idsa.businesslogic.processor.sender.SenderFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderMapIDSCP2toMultipart;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataFromDAppProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorBodyFormData;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorHttpHeader;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedResponseMessage;
import it.eng.idsa.businesslogic.processor.sender.SenderReadAndSaveFileProcessor;
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

	private static final Logger logger = LogManager.getLogger(CamelRouteSender.class);

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
	SenderMapIDSCP2toMultipart senderMapIDSCP2toMultipart;
	
	

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
	private IdsMessageTypeExtractionProcessor idsMessageTypeExtractionProcessor;
	@Autowired
	private SenderReadAndSaveFileProcessor senderReadAndSaveFileProcessor;
	@Autowired
	ReceiverMapMultipartToIDSCP2 receiverMapMultipartToIDSCP2;
	
	
	
	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Value("${application.idscp2.isEnabled}")
	private boolean isEnabledIdscp2;
	
	@Value("${application.isReceiver}")
	private boolean receiver;
	
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
				.process(createRegistratioMessageSender)
			.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/update")
				.process(createUpdateMessageSender)
			.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/delete")
				.process(createDeleteMessageSender)
			.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/passivate")
				.process(createPassivateMessageSender)
			.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/selfRegistration/query")
				.process(createBrokerQueryMessageSender)
			.to("direct:registrationProcess");
			
			from("direct:registrationProcess")
				.process(getTokenFromDapsProcessor)
				.process(sendDataToBusinessLogicProcessor)
				.process(parseReceivedResponseMessage)
				.process(validateTokenProcessor)
				.process(sendResponseToDataAppProcessor);
			
						
			if(isEnabledIdscp2 && !receiver) {
			// Camel SSL - Endpoint: A - Body binary
			logger.info("Starting IDSCP v2 client sender route");
			
            from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyBinary")
            	.log("##### STARTING IDSCP2 ARTIFACT-GIVEN MESSAGE FLOW #####")
            	.process(parseReceivedDataProcessorBodyBinary)
            	.toD("${exchangeProperty.domain}")
            	.process(senderMapIDSCP2toMultipart)
            	.process(registerTransactionToCHProcessor)
            	.delay()
                .constant(5000);
            
            logger.info("Starting IDSCP v2 client receiver route");
            from("idscp2client://localhost:29292?connectionShareId=pingPongConnection&sslContextParameters=#sslContext&useIdsMessages=true")
        		.process(idsMessageTypeExtractionProcessor)
        		.log("### CLIENT RECEIVER: Detected Message type: ${exchangeProperty.ids-type}")
        		.choice()
        			.when()
        			.simple("${exchangeProperty.ids-type} == 'ArtifactResponseMessage'")
        			.log("### Handle ArtifactResponseMessage ###")
        			
        			.process(senderMapIDSCP2toMultipart)
        			
                    .process(registerTransactionToCHProcessor)
                    .process(senderUsageControlProcessor)                    
                    .process(sendResponseToDataAppProcessor)
        			//TODO delete it, just for test purpose
        	   		//.process(senderReadAndSaveFileProcessor)
        	   		.removeHeader("idscp2-header")
        	   		.setBody().simple("${null}")
        	   		.endChoice()
        	   		.otherwise()
        	   				.log("### Client received (otherwise branch):\n${body}\n### Header: ###\n${headers[idscp2-header]}")
        	   		        .removeHeader("idscp2-header")
        	   		        .setBody().simple("${null}");
        	
          
            	/***************EXAMPLES***************/
		               
		   		 //sending client 
		   	/*	 from("timer://tenSecondsTimer?fixedRate=true&period=10000")
		   		 .id("idscp2SenderClient")
		            .setBody()
		                .simple("PING")
		            .setHeader("idscp2.type")
		                .simple("ping")
		            .log("Client sends: ${body} (idscp2.type: ${headers[idscp2.type]})")
		            .to("idscp2client://localhost:29292?connectionShareId=pingPongConnection&sslContextParameters=#serverSslContext");
		   		 
		   		 from("idscp2client://localhost:29292?connectionShareId=pingPongConnection&sslContextParameters=#serverSslContext")
		   		 .id("idscp2ReceiverClient")
		   		 .log("Client received: ${body} (idscp2.type: ${headers[idscp2.type]})")
		            .removeHeader("idscp2.type");*/
		   		 
		   		 
		   		 //active (sending) server
		   		/* from("idscp2client://localhost:29292?sslContextParameters=#serverSslContext")
		   		 .id("idscp2ReceiverClient")
		   		 .log("Client received: ${body} (idscp2.type: ${headers[idscp2.type]})")
		            .removeHeader("idscp2.type");*/
            
            
            }else {
				from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyBinary")
        		.process(parseReceivedDataProcessorBodyBinary)
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

            // Camel SSL - Endpoint: A - Body form-data
            from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyFormData")
				.process(parseReceivedDataProcessorBodyFormData)
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
            
         // Camel SSL - Endpoint: A - Http-header
            from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageHttpHeader" + "?httpMethodRestrict=POST")
        		.process(parseReceivedDataProcessorHttpHeader)
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
            } else {
				// End point A. Communication between Data App and ECC Sender.
				//fixedRate=true&period=10s
				from("timer://timerEndpointA?repeatCount=-1") //EndPoint A
					.process(fileRecreatorProcessor)
					.process(parseReceivedDataFromDAppProcessorBodyBinary)
					.process(getTokenFromDapsProcessor)
	                .process(registerTransactionToCHProcessor)
					// Send data to Endpoint B
					.process(sendDataToBusinessLogicProcessor)
					.process(parseReceivedResponseMessage)
					.process(validateTokenProcessor)
	                .process(registerTransactionToCHProcessor)
	                .process(senderUsageControlProcessor)
					.process(sendResponseToDataAppProcessor);
			//@formatter:on
		}
	}

}