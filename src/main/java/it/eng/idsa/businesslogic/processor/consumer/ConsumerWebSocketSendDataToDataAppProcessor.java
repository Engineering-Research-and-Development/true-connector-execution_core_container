package it.eng.idsa.businesslogic.processor.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.producer.websocket.client.MessageWebSocketOverHttpSender;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonio Scatoloni
 */

@Component
public class ConsumerWebSocketSendDataToDataAppProcessor implements Processor {

    private static final Logger logger = LogManager.getLogger(ConsumerWebSocketSendDataToDataAppProcessor.class);

    @Value("${application.openDataAppReceiver}")
    private String openDataAppReceiver;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private MultipartMessageService multipartMessageService;

    @Autowired
    private RejectionMessageService rejectionMessageService;

    @Autowired
    private MessageWebSocketOverHttpSender messageWebSocketOverHttpSender;

    @Override
    public void process(Exchange exchange) throws Exception {

        Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);

        // Get header, payload and message
        String header = filterHeader(multipartMessageParts.get("header").toString());
        String payload = null;
        if (multipartMessageParts.containsKey("payload")) {
            payload = multipartMessageParts.get("payload").toString();
        }
        Message message = multipartMessageService.getMessage(multipartMessageParts.get("header"));
        URL openDataAppReceiverRouterUrl = new URL(openDataAppReceiver);
        String response = messageWebSocketOverHttpSender
                .sendMultipartMessageWebSocketOverHttps(openDataAppReceiverRouterUrl.getHost(), openDataAppReceiverRouterUrl.getPort(),
                        openDataAppReceiverRouterUrl.getPath(), header, payload);
        // Handle response
        handleResponse(exchange, message, response, configuration.getOpenDataAppReceiver());

    }


    private String filterHeader(String header) throws JsonMappingException, JsonProcessingException {
        Message message = multipartMessageService.getMessage(header);
        return multipartMessageService.removeToken(message);
    }

    private void handleResponse(Exchange exchange, Message message, String response, String openApiDataAppAddress) throws UnsupportedOperationException, IOException {
        if (response == null) {
            logger.info("...communication error with: " + openApiDataAppAddress);
            rejectionMessageService.sendRejectionMessage(
                    RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
                    message);
        } else {
            logger.info("content type response received from the DataAPP=" + response);
            logger.info("response received from the DataAPP=" + response);
            logger.info("Successful response: " + response);
            String header = multipartMessageService.getHeaderContentString(response);
            String payload = multipartMessageService.getPayloadContent(response);
            exchange.getMessage().setHeader("header", header);
            if (payload != null) {
                exchange.getMessage().setHeader("payload", payload);
            }
        }
    }
}
