package it.eng.idsa.businesslogic.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.processor.consumer.ConsumerExceptionMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerMultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerSendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerValidateTokenProcessor;
import it.eng.idsa.businesslogic.processor.consumer.ConsumerWebSocketSendDataToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorConsumer;
import it.eng.idsa.businesslogic.processor.exception.ExceptionProcessorProducer;
import it.eng.idsa.businesslogic.processor.producer.ProducerFileRecreatorProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerGetTokenFromDapsProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedDataFromDAppProcessorBodyBinary;
import it.eng.idsa.businesslogic.processor.producer.ProducerParseReceivedResponseMessage;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendDataToBusinessLogicProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendResponseToDataAppProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerSendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.producer.ProducerValidateTokenProcessor;

/**
 * @author Antonio Scatoloni
 */

@Component
public class CamelRouteWebSocket extends RouteBuilder {

    private static final Logger logger = LogManager.getLogger(CamelRouteWebSocket.class);

    @Autowired
    ProducerParseReceivedDataFromDAppProcessorBodyBinary producerParseReceivedDataFromDAppProcessorBodyBinary;

    @Autowired
    ProducerGetTokenFromDapsProcessor producerGetTokenFromDapsProcessor;

    @Autowired
    ConsumerGetTokenFromDapsProcessor consumerGetTokenFromDapsProcessor;

    @Autowired
    ProducerSendTransactionToCHProcessor producerSendTransactionToCHProcessor;

    @Autowired
    ConsumerSendTransactionToCHProcessor consumerSendTransactionToCHProcessor;

    @Autowired
    ConsumerSendDataToBusinessLogicProcessor consumerSendDataToBusinessLogicProcessor;

    @Autowired
    ProducerSendDataToBusinessLogicProcessor producerSendDataToBusinessLogicProcessor;

    @Autowired
    ProducerParseReceivedResponseMessage parseReceivedResponseMessage;

    @Autowired
    ProducerValidateTokenProcessor producerValidateTokenProcessor;

    @Autowired
    ConsumerValidateTokenProcessor consumerValidateTokenProcessor;

    @Autowired
    ProducerSendResponseToDataAppProcessor sendResponseToDataAppProcessor;

    @Autowired
    ExceptionProcessorProducer processorException;

    @Autowired
    ConsumerMultiPartMessageProcessor multiPartMessageProcessor;

    @Autowired(required = false)
    ConsumerFileRecreatorProcessor consumerFileRecreatorProcessor;

    @Autowired(required = false)
    ProducerFileRecreatorProcessor producerFileRecreatorProcessor;

    @Autowired
    ConsumerSendDataToDataAppProcessor consumerSendDataToDataAppProcessor;

    @Autowired
    ProducerSendResponseToDataAppProcessor producerSendResponseToDataAppProcessor;

    @Autowired
    ExceptionProcessorConsumer exceptionProcessorConsumer;

    @Autowired
    ConsumerExceptionMultiPartMessageProcessor exceptionMultiPartMessageProcessor;

    @Autowired
    ConsumerWebSocketSendDataToDataAppProcessor sendDataToDataAppProcessorOverWS;

    @Value("${application.idscp.isEnabled}")
    private boolean isEnabledIdscp;

    @Value("${application.websocket.isEnabled}")
    private boolean isEnabledWebSocket;

    @Value("${application.dataApp.websocket.isEnabled}")
    private boolean isEnabledDataAppWebSocket;


    @Override
    public void configure() throws Exception {
        if (isEnabledIdscp || isEnabledWebSocket)
        	// End point B. ECC communication (Web Socket or IDSCP)
            from("timer://timerEndpointB?fixedRate=true&period=10s") //EndPoint B
            	.process(consumerFileRecreatorProcessor)
            	.process(multiPartMessageProcessor)
                .choice()
                	.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
                    	.process(consumerValidateTokenProcessor)
                        // Send to the Endpoint: F
                        .choice()
                        	.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
                            	.process(sendDataToDataAppProcessorOverWS)
                            .when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
                            	.process(consumerSendDataToDataAppProcessor)
                        .endChoice()
                        .process(multiPartMessageProcessor)
                        .process(consumerGetTokenFromDapsProcessor)
                        .process(consumerSendDataToBusinessLogicProcessor)
                        .choice()
                        	.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                            	.process(consumerSendTransactionToCHProcessor)
                        .endChoice()
                    .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
                        // Send to the Endpoint: F
                        .choice()
                        	.when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(true))
                            	.process(sendDataToDataAppProcessorOverWS)
                            .when(header("Is-Enabled-DataApp-WebSocket").isEqualTo(false))
                            	.process(consumerSendDataToDataAppProcessor)
                        .endChoice()
                        .process(multiPartMessageProcessor)
                        .process(consumerSendDataToBusinessLogicProcessor)
                        .choice()
                        	.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                        	// .process(consumerSendTransactionToCHProcessor)
                        .endChoice()
                .endChoice();
        if (isEnabledDataAppWebSocket)
        	// End point A. Coomunication between Data App and ECC Producer.
            from("timer://timerEndpointA?fixedRate=true&period=10s") //EndPoint A
                    .process(producerFileRecreatorProcessor)
                    .process(producerParseReceivedDataFromDAppProcessorBodyBinary)
                    .choice()
                    	.when(header("Is-Enabled-Daps-Interaction").isEqualTo(true))
                        	.process(producerGetTokenFromDapsProcessor)
                        	// Send data to Endpoint B
                        	.process(producerSendDataToBusinessLogicProcessor)
                        	.process(parseReceivedResponseMessage)
                        	.process(producerValidateTokenProcessor)
                        	//.process(sendResponseToDataAppProcessor)
                        	.process(producerSendResponseToDataAppProcessor)
                        	.choice()
                        		.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                        			.process(producerSendTransactionToCHProcessor)
                        	.endChoice()
                        .when(header("Is-Enabled-Daps-Interaction").isEqualTo(false))
                        	// Send data to Endpoint B
                        	.process(producerSendDataToBusinessLogicProcessor)
                        	.process(parseReceivedResponseMessage)
                        	//.process(sendResponseToDataAppProcessor)
                        	.process(producerSendResponseToDataAppProcessor)
                        	.choice()
                        		.when(header("Is-Enabled-Clearing-House").isEqualTo(true))
                        			.process(producerSendTransactionToCHProcessor)
                        	.endChoice()
                    .endChoice();
    }
}


