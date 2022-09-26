/**
 *
 */
package it.eng.idsa.businesslogic.service.impl;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.HashFileService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.clearinghouse.model.Body;
import it.eng.idsa.clearinghouse.model.NotificationContent;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@Service
@Transactional
public class ClearingHouseServiceImpl implements ClearingHouseService {
	private static final Logger logger = LoggerFactory.getLogger(ClearingHouseServiceImpl.class);

	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	
	@Autowired
	private DapsTokenProviderService dapsProvider;

    @Autowired
    private HashFileService hashService;

    @Autowired
    private SendDataToBusinessLogicService sendDataToBusinessLogicService;

    @Override
    public boolean registerTransaction(Message correlatedMessage, String payload) {
        String messageLogEndpoint = "messages/log/";
        String processEndpoint = "process/";

        boolean success = false;
        try {
            logger.info("registerTransaction...");
            String endpoint;
            String pid;

            // Searches if a process has already been created
            boolean test = correlatedMessage.getTransferContract() != null;
            if (test) {
                //TODO write logic
                pid = extractPIDfromContract(correlatedMessage);

                //TODO write logic
                if (alreadyExists(pid)) {
                    //TODO write logic
                    createProcess(correlatedMessage, processEndpoint, pid);
                } else {
                    //TODO write logic
                    createProcess(correlatedMessage, processEndpoint, pid);
                }
            } else {
                //default random PID
                pid = createPID(correlatedMessage);
            }

            endpoint = configuration.getClearingHouseUrl() + messageLogEndpoint + pid; //Create Message for Clearing House

			LogMessage logInfo = new LogMessageBuilder()
					._modelVersion_(UtilMessageService.MODEL_VERSION)
					._issuerConnector_(whoIAm())
					._issued_(DateUtil.now())
					._senderAgent_(correlatedMessage.getSenderAgent())
					._securityToken_(dapsProvider.getDynamicAtributeToken())
					.build();

            String hash = hashService.hash(payload);
            NotificationContent notificationContent = createNotificationContent(logInfo, correlatedMessage, hash);

            Serializer serializer = new Serializer();
            Message notificationContentHeader = notificationContent.getHeader();
            Body notificationContentBody = notificationContent.getBody();

            String msgPayload = serializer.serialize(notificationContentBody);
            MultipartMessage multipartMessage = new MultipartMessageBuilder().withHeaderContent(notificationContentHeader)
                                                                             .withPayloadContent(msgPayload)
                                                                             .build();

            LoggerCHMessage chMessage = new LoggerCHMessage(notificationContentHeader, notificationContentBody);
            String msgSerialized = "Serialized Message which is sending to CH=" + serializer.serialize(chMessage);
            logger.info(msgSerialized);
            String sendingDataInfo = "Sending Data to the Clearing House " + endpoint + " ...";
            logger.info(sendingDataInfo);
            sendDataToBusinessLogicService.sendMessageFormData(endpoint, multipartMessage, new HashMap<>());


            String logMessageIdInfo = "Data [LogMessage.id=" + logInfo.getId() + "] sent to the Clearing House " + endpoint;
            logger.info(logMessageIdInfo);
            hashService.recordHash(hash, payload, notificationContent);

			success = true;
		} catch (Exception e) {
			logger.error("Could not register the following message to clearing house", e);
		}

		return success;
	}

	private URI whoIAm() {
		return selfDescriptionConfiguration.getConnectorURI();
	}

    private boolean alreadyExists(String pid) {
        //TODO search process in fCH

        return Boolean.parseBoolean(pid); //fake return - it will be changed
    }

    private void createProcess(Message correlatedMessage, String endpoint, String pid) throws UnsupportedEncodingException {
        //TODO call processCreate endpoint
        String processEndpoint = configuration.getClearingHouseUrl() + endpoint + pid; //Create a process in CH

        RequestMessage processMessage = new RequestMessageBuilder()
                ._modelVersion_(UtilMessageService.MODEL_VERSION)
                ._issuerConnector_(whoIAm())
                ._issued_(DateUtil.now())
                ._senderAgent_(correlatedMessage.getSenderAgent())
                ._securityToken_(dapsProvider.getDynamicAtributeToken())
                .build();

        //TODO need MultipartMessage
        MultipartMessage multipartMessage = new MultipartMessageBuilder().withHeaderContent(processMessage)
                                                                         .withPayloadContent("")
                                                                         .build();
        sendDataToBusinessLogicService.sendMessageFormData(processEndpoint, multipartMessage, new HashMap<>());
    }

    private String extractPIDfromContract(Message correlatedMessage) {
        //TODO
        String[] strings = correlatedMessage.getTransferContract().getPath().split("/");
        return strings[strings.length - 1];
    }

    private static String createPID(Message correlatedMessage) {
        ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();
        return uuidGenerator.generateId(correlatedMessage).toString();
    }

    @NotNull
    private static NotificationContent createNotificationContent(LogMessage logInfo, Message correlatedMessage, String hash) {
        NotificationContent notificationContent = new NotificationContent();

        // Header Management
        notificationContent.setHeader(logInfo);
        //explicity setHeader because notificationContent.setHeader return always "DummyTokenValue"
        notificationContent.getHeader().setSecurityToken(logInfo.getSecurityToken());

        notificationContent.setBody(getBodyFromMessageAndHash(correlatedMessage, hash));
        return notificationContent;
    }

    @NotNull
    private static Body getBodyFromMessageAndHash(Message correlatedMessage, String hash) {
        Body body = new Body();
        body.setHeader(correlatedMessage);
        body.setPayload(hash);
        return body;
    }


    private static class LoggerCHMessage {
        private final Message header;
        private final Body payload;

        public LoggerCHMessage(Message header, Body body) {
            this.header = header;
            this.payload = body;
        }

        public Message getHeader() {
            return header;
        }

        public Body getPayload() {
            return payload;
        }
    }
}

