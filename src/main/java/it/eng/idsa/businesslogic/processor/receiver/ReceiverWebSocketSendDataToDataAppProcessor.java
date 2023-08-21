package it.eng.idsa.businesslogic.processor.receiver;

import java.io.IOException;
import java.net.URL;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.sender.websocket.client.MessageWebSocketOverHttpSender;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.Helper;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * @author Antonio Scatoloni
 */

@Component
public class ReceiverWebSocketSendDataToDataAppProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(ReceiverWebSocketSendDataToDataAppProcessor.class);

    @Value("${application.openDataAppReceiver}")
    private String openDataAppReceiver;

    @Autowired
    private ApplicationConfiguration configuration;

    @Autowired
    private RejectionMessageService rejectionMessageService;

    @Autowired
    private MessageWebSocketOverHttpSender messageWebSocketOverHttpSender;

    @Value("#{new Boolean('${application.isEnabledUsageControl}')}")
    private boolean isEnabledUsageControl;

    @Override
    @CamelAuditable(beforeEventType =  TrueConnectorEventType.CONNECTOR_SEND_DATAAPP,
	successEventType = TrueConnectorEventType.CONNECTOR_RESPONSE, 
	failureEventType = TrueConnectorEventType.EXCEPTION_SERVER_ERROR)
    public void process(Exchange exchange) throws Exception {
        MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);

        // Get header, payload and message
        String header = multipartMessage.getHeaderContentString();
        String payload = null;
        payload = multipartMessage.getPayloadContent();
        Message message = multipartMessage.getHeaderContent();
        URL openDataAppReceiverRouterUrl = new URL(openDataAppReceiver);
        String response = messageWebSocketOverHttpSender
                .sendMultipartMessageWebSocketOverHttps(openDataAppReceiverRouterUrl.getHost(), openDataAppReceiverRouterUrl.getPort(),
                        openDataAppReceiverRouterUrl.getPath(), header, payload);
        // Handle response
        handleResponse(exchange, message, response, configuration.getOpenDataAppReceiver());
      }	

      private void handleResponse(Exchange exchange, Message message, String response, String openApiDataAppAddress) throws UnsupportedOperationException, IOException {
          if (response == null) {
              logger.info("...communication error with: " + openApiDataAppAddress);
              rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.INTERNAL_RECIPIENT_ERROR);
          } else {
			  MultipartMessage multipartMessage = MultipartMessageProcessor.parseMultipartMessage(response);
              exchange.getMessage().setHeader(MessagePart.HEADER, multipartMessage.getHeaderContentString());
              //Save original Header for Usage Control Enforcement
              logger.info("Received response from DataAPP, message type: {}", Helper.getIDSMessageType(multipartMessage.getHeaderContent()));
              if (multipartMessage.getPayloadContent() != null) {
                  exchange.getMessage().setHeader(MessagePart.PAYLOAD, multipartMessage.getPayloadContent());
              }
  			exchange.getMessage().setBody(multipartMessage);
          }
      }
  }