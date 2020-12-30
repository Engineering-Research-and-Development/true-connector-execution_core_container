package it.eng.idsa.businesslogic.processor.receiver;

import java.io.IOException;
import java.net.URL;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.sender.websocket.client.MessageWebSocketOverHttpSender;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * @author Antonio Scatoloni
 */

@Component
public class ReceiverWebSocketSendDataToDataAppProcessor implements Processor {

    private static final Logger logger = LogManager.getLogger(ReceiverWebSocketSendDataToDataAppProcessor.class);

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

    @Value("${application.isEnabledUsageControl:false}")
    private boolean isEnabledUsageControl;

    private String originalHeader;

    @Override
    public void process(Exchange exchange) throws Exception {
        MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);

        // Get header, payload and message
        String header = filterHeader(multipartMessage.getHeaderContentString());
        String payload = null;
        this.originalHeader = header;
        payload = multipartMessage.getPayloadContent();
        Message message = multipartMessage.getHeaderContent();
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
        	  logger.info("Received response from DataAPP");
        	  logger.debug("response received from the DataAPP=" + response);
        	  String header = multipartMessageService.getHeaderContentString(response);
              String payload = multipartMessageService.getPayloadContent(response);
              exchange.getMessage().setHeader("header", header);
              //Save original Header for Usage Control Enforcement
              if(isEnabledUsageControl) {
                  exchange.getMessage().setHeader("Original-Message-Header", originalHeader);
              }
              if (payload != null) {
                  exchange.getMessage().setHeader("payload", payload);
              }
          }
      }
  }