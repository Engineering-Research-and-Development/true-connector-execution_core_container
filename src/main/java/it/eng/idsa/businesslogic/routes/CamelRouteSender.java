package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.common.RegisterTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorReceiver;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorSender;
import it.eng.idsa.businesslogic.processor.sender.SenderFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataFromDAppProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorBodyFormData;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedDataProcessorHttpHeader;
import it.eng.idsa.businesslogic.processor.sender.SenderParseReceivedResponseMessage;
import it.eng.idsa.businesslogic.processor.sender.SenderProcessRegistrationResponseProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderSendRegistrationRequestProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderSendResponseToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderUsageControlProcessor;
import it.eng.idsa.businesslogic.processor.sender.SenderValidateTokenProcessor;
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
	SenderGetTokenFromDapsProcessor getTokenFromDapsProcessor;

	@Autowired
	RegisterTransactionToCHProcessor registerTransactionToCHProcessor;

	@Autowired
	SenderSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;

	@Autowired
	SenderParseReceivedResponseMessage parseReceivedResponseMessage;

	@Autowired
	SenderValidateTokenProcessor validateTokenProcessor;

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
	private SenderSendRegistrationRequestProcessor sendRegistrationRequestProcessor;

	@Autowired
	private SenderProcessRegistrationResponseProcessor processRegistrationResponseSender;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

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
			.process(sendRegistrationRequestProcessor)
			//TODO following processor is workaround 
			// to remove Content-Type from response in order to be able to Serialize it correct
			.process(processRegistrationResponseSender)
			.process(parseReceivedResponseMessage)
			.process(sendResponseToDataAppProcessor);

			// Camel SSL - Endpoint: A - Body binary
            from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyBinary")
                    .process(parseReceivedDataProcessorBodyBinary)
                    .choice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
                            .process(getTokenFromDapsProcessor)
                            .process(registerTransactionToCHProcessor)
                             // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(validateTokenProcessor)
	                        .process(registerTransactionToCHProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(senderUsageControlProcessor)
							.removeHeaders("Camel*")
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
		                    .process(registerTransactionToCHProcessor)
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
	                        .process(registerTransactionToCHProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(senderUsageControlProcessor)
							.removeHeaders("Camel*")
                    .endChoice();

            // Camel SSL - Endpoint: A - Body form-data
            from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageBodyFormData")
                    .process(parseReceivedDataProcessorBodyFormData)
                    .choice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
                            .process(getTokenFromDapsProcessor)
	                        .process(registerTransactionToCHProcessor)
                             // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(validateTokenProcessor)
	                        .process(registerTransactionToCHProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(senderUsageControlProcessor)
							.removeHeaders("Camel*")
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
		                    .process(registerTransactionToCHProcessor)
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
	                        .process(registerTransactionToCHProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(senderUsageControlProcessor)
							.removeHeaders("Camel*")

                    .endChoice();
            
         // Camel SSL - Endpoint: A - Http-header
            from("jetty://https4://0.0.0.0:" + configuration.getCamelSenderPort() + "/incoming-data-app/multipartMessageHttpHeader" + "?httpMethodRestrict=POST")
                    .process(parseReceivedDataProcessorHttpHeader)
                    .choice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
                            .process(getTokenFromDapsProcessor)
	                        .process(registerTransactionToCHProcessor)
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(validateTokenProcessor)
	                        .process(registerTransactionToCHProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(senderUsageControlProcessor)
							.removeHeaders("Camel*")
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
		                    .process(registerTransactionToCHProcessor)
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
	                        .process(registerTransactionToCHProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(senderUsageControlProcessor)
							.removeHeaders("Camel*")
                    .endChoice();
            } else {
				// End point A. Communication between Data App and ECC Sender.
				//fixedRate=true&period=10s
				from("timer://timerEndpointA?repeatCount=-1") //EndPoint A
						.process(fileRecreatorProcessor)
						.process(parseReceivedDataFromDAppProcessorBodyBinary)
						.choice()
							.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
								.process(getTokenFromDapsProcessor)
	                            .process(registerTransactionToCHProcessor)
								// Send data to Endpoint B
								.process(sendDataToBusinessLogicProcessor)
								.process(parseReceivedResponseMessage)
								.process(validateTokenProcessor)
	                            .process(registerTransactionToCHProcessor)
								.process(sendResponseToDataAppProcessor)
								.process(senderUsageControlProcessor)
							.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
		                        .process(registerTransactionToCHProcessor)
								// Send data to Endpoint B
								.process(sendDataToBusinessLogicProcessor)
								.process(parseReceivedResponseMessage)
	                            .process(registerTransactionToCHProcessor)
								.process(sendResponseToDataAppProcessor)
								.process(senderUsageControlProcessor)
					.endChoice();
			//@formatter:on
		}
	}

}