package it.eng.idsa.businesslogic.routes;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorConsumer;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorProducer;
import it.eng.idsa.businesslogic.processor.producer.*;
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
	CamelContext camelContext;

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
            // Camel SSL - Endpoint: A - Body binary
            from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/incoming-data-app/multipartMessageBodyBinary")
                    .process(parseReceivedDataProcessorBodyBinary)
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
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                .process(sendTransactionToCHProcessor)
                            .endChoice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
    //						.process(sendToActiveMQ)
    //						.process(receiveFromActiveMQ)
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(sendResponseToDataAppProcessor)
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                    .process(sendTransactionToCHProcessor)
                            .endChoice()
                    .endChoice();

            // Camel SSL - Endpoint: A - Body form-data
            from("jetty://https4://0.0.0.0:" + configuration.getCamelProducerPort() + "/incoming-data-app/multipartMessageBodyFormData")
                    .process(parseReceivedDataProcessorBodyFormData)
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
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                    .process(sendTransactionToCHProcessor)
                            .endChoice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
        //					.process(sendToActiveMQ)
        //					.process(receiveFromActiveMQ)
                            // Send data to Endpoint B
                            .process(sendDataToBusinessLogicProcessor)
                            .process(parseReceivedResponseMessage)
                            .process(sendResponseToDataAppProcessor)
                            .choice()
                                .when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                                    .process(sendTransactionToCHProcessor)
                            .endChoice()
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
								.choice()
								.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
									.process(sendTransactionToCHProcessor)
								.endChoice()
									.when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
										// Send data to Endpoint B
										.process(sendDataToBusinessLogicProcessor)
										.process(parseReceivedResponseMessage)
										//.process(sendResponseToDataAppProcessor)
										.process(sendResponseToDataAppProcessor)
								.choice()
									.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
									.process(sendTransactionToCHProcessor)
								.endChoice()
					.endChoice();
			//@formatter:on
		}
	}

}
