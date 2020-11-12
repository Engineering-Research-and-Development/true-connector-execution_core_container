package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorConsumer;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorProducer;
import it.eng.idsa.businesslogic.processor.producer.ProducerFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedDataFromDAppProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedDataProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedDataProcessorBodyFormData;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedDataProcessorHttpHeader;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedResponseMessage;
import it.eng.idsa.businesslogic.processor.producer.ProducerProcessRegistrationResponseProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendRegistrationRequestProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendResponseToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerUsageControlProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.producer.registration.ProducerCreateDeleteMessageProcessor;
import it.eng.idsa.businesslogic.processor.producer.registration.ProducerCreatePassivateMessageProcessor;
import it.eng.idsa.businesslogic.processor.producer.registration.ProducerCreateQueryBrokerMessageProcessor;
import it.eng.idsa.businesslogic.processor.producer.registration.ProducerCreateRegistrationMessageProcessor;
import it.eng.idsa.businesslogic.processor.producer.registration.ProducerCreateUpdateMessageProcessor;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class CamelRouteProducer extends RouteBuilder {

	private static final Logger logger = LogManager.getLogger(CamelRouteProducer.class);

	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired(required = false)
	ProducerFileRecreatorProcessor fileRecreatorProcessor;

	@Autowired
	ProducerParseReceivedDataProcessorBodyBinary parseReceivedDataProcessorBodyBinary;

	@Autowired
	ProducerParseReceivedDataProcessorBodyFormData parseReceivedDataProcessorBodyFormData;
	
	@Autowired
	ProducerParseReceivedDataProcessorHttpHeader parseReceivedDataProcessorHttpHeader;

	@Autowired
	ProducerGetTokenFromDapsProcessor getTokenFromDapsProcessor;

	@Autowired
	ProducerSendTransactionToCHProcessor sendTransactionToCHProcessor;

	@Autowired
	ProducerSendDataToBusinessLogicProcessor sendDataToBusinessLogicProcessor;

	@Autowired
	ProducerParseReceivedResponseMessage parseReceivedResponseMessage;

	@Autowired
	ProducerValidateTokenProcessor validateTokenProcessor;

	@Autowired
	ProducerSendResponseToDataAppProcessor sendResponseToDataAppProcessor;

	@Autowired
	ExceptionProcessorProducer processorException;

	@Autowired
	ProducerParseReceivedDataFromDAppProcessorBodyBinary parseReceivedDataFromDAppProcessorBodyBinary;

	@Autowired
	ExceptionProcessorConsumer exceptionProcessorConsumer;

	@Autowired
	ProducerUsageControlProcessor producerUsageControlProcessor;

	@Autowired
	CamelContext camelContext;
	
	@Autowired
	private ProducerCreateRegistrationMessageProcessor createRegistratioMessageProducer;
	@Autowired
	private ProducerCreateUpdateMessageProcessor createUpdateMessageProducer;
	@Autowired
	private ProducerCreateDeleteMessageProcessor createDeleteMessageProducer;
	@Autowired
	private ProducerCreatePassivateMessageProcessor createPassivateMessageProducer;
	@Autowired
	private ProducerCreateQueryBrokerMessageProcessor createBrokerQueryMessageProducer;

	@Autowired
	private ProducerSendRegistrationRequestProcessor sendRegistrationRequestProcessor;

	@Autowired
	private ProducerProcessRegistrationResponseProcessor processRegistrationResponseProducer;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Override
	public void configure() throws Exception {
		logger.debug("Starting Camel Routes...producer side");

		camelContext.getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
		camelContext.getShutdownStrategy().setTimeout(3);
		//@formatter:off
		onException(ExceptionForProcessor.class, RuntimeException.class)
			.handled(true)
			.process(processorException);
		

		if(!isEnabledDataAppWebSocket) {
			from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/selfRegistration/register")
			.process(createRegistratioMessageProducer)
			.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/selfRegistration/update")
			.process(createUpdateMessageProducer)
			.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/selfRegistration/delete")
			.process(createDeleteMessageProducer)
			.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/selfRegistration/passivate")
			.process(createPassivateMessageProducer)
			.to("direct:registrationProcess");
			from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/selfRegistration/query")
			.process(createBrokerQueryMessageProducer)
			.to("direct:registrationProcess");
			
			from("direct:registrationProcess")
			.process(sendRegistrationRequestProcessor)
			//TODO following processor is workaround 
			// to remove Content-Type from response in order to be able to Serialize it correct
			.process(processRegistrationResponseProducer)
			.process(parseReceivedResponseMessage)
			.process(sendResponseToDataAppProcessor);

			// Camel SSL - Endpoint: A - Body binary
            from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/incoming-data-app/multipartMessageBodyBinary")
                    .process(parseReceivedDataProcessorBodyBinary)
                    .choice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
                            .process(getTokenFromDapsProcessor)
                             // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(validateTokenProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(producerUsageControlProcessor)
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                .process(sendTransactionToCHProcessor)
                            .endChoice()
							.removeHeaders("Camel*")
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(sendResponseToDataAppProcessor)
                            .process(producerUsageControlProcessor)
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                    .process(sendTransactionToCHProcessor)
                            .endChoice()
							.removeHeaders("Camel*")
                    .endChoice();

            // Camel SSL - Endpoint: A - Body form-data
            from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/incoming-data-app/multipartMessageBodyFormData")
                    .process(parseReceivedDataProcessorBodyFormData)
                    .choice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
                            .process(getTokenFromDapsProcessor)
                             // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(validateTokenProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(producerUsageControlProcessor)
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                    .process(sendTransactionToCHProcessor)
                            .endChoice()
							.removeHeaders("Camel*")
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(sendResponseToDataAppProcessor)
                            .process(producerUsageControlProcessor)
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                    .process(sendTransactionToCHProcessor)
                            .endChoice()
							.removeHeaders("Camel*")

                    .endChoice();
            
         // Camel SSL - Endpoint: A - Http-header
            from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/incoming-data-app/multipartMessageHttpHeader" + "?httpMethodRestrict=POST")
                    .process(parseReceivedDataProcessorHttpHeader)
                    .choice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
                            .process(getTokenFromDapsProcessor)
    //						.process(sendToActiveMQ)
    //						.process(receiveFromActiveMQ)
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(validateTokenProcessor)
                            .process(sendResponseToDataAppProcessor)
                            .process(producerUsageControlProcessor)
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                    .process(sendTransactionToCHProcessor)
                            .endChoice()
							.removeHeaders("Camel*")
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
        //					.process(sendToActiveMQ)
        //					.process(receiveFromActiveMQ)
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(sendResponseToDataAppProcessor)
                            .process(producerUsageControlProcessor)
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                    .process(sendTransactionToCHProcessor)
                            .endChoice()
							.removeHeaders("Camel*")
                    .endChoice();
            } else {
				// End point A. Communication between Data App and ECC Producer.
				//fixedRate=true&period=10s
				from("timer://timerEndpointA?repeatCount=-1") //EndPoint A
						.process(fileRecreatorProcessor)
						.process(parseReceivedDataFromDAppProcessorBodyBinary)
						.choice()
							.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
								.process(getTokenFromDapsProcessor)
								// Send data to Endpoint B
								.process(sendDataToBusinessLogicProcessor)
								.process(parseReceivedResponseMessage)
								.process(validateTokenProcessor)
								//.process(sendResponseToDataAppProcessor)
								.process(sendResponseToDataAppProcessor)
								.process(producerUsageControlProcessor)
								.choice()
								.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
									.process(sendTransactionToCHProcessor)
								.endChoice()
									.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
										// Send data to Endpoint B
										.process(sendDataToBusinessLogicProcessor)
										.process(parseReceivedResponseMessage)
										.process(sendResponseToDataAppProcessor)
										.process(producerUsageControlProcessor)
								.choice()
									.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
									.process(sendTransactionToCHProcessor)
								.endChoice()
					.endChoice();
			//@formatter:on
		}
	}

}