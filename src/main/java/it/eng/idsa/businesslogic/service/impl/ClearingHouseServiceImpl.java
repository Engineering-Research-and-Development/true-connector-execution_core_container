/**
 *
 */
package it.eng.idsa.businesslogic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

			if (correlatedMessage.getTransferContract() != null) {
				//log all exchanged messages relating to same ContractAgreement in same process
				pid = extractPIDfromContract(correlatedMessage);
				createProcessPID(correlatedMessage, processEndpoint, pid);
			} else {
				//default random PID
				pid = createPID();
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
			String msgSerialized = "Serialized Message which is sending to CH=\n" + serializer.serialize(chMessage);
			logger.info(msgSerialized);
			String sendingDataInfo = "Sending Data to the Clearing House " + endpoint + " ...";
			logger.info(sendingDataInfo);
			Response response = sendDataToBusinessLogicService.sendMessageFormData(endpoint, multipartMessage, new HashMap<>());


			String logMessageIdInfo = "Data [LogMessage.id=" + logInfo.getId() + "] sent to the Clearing House " + endpoint;

			logger.info(logMessageIdInfo);
			hashService.recordHash(hash, payload, notificationContent);

			if (response.code() == 201) {
				success = true;
			} else {
				String errorMessage = "Clearing house registered fails.\nRejectionReason: " + response.code() + " " + response.message();
				logger.error(errorMessage);
			}

		} catch (Exception e) {
			logger.error("Could not register the following message to clearing house", e);
		}

		return success;
	}

	private URI whoIAm() {
		return selfDescriptionConfiguration.getConnectorURI();
	}

	private String createPID() {
		return UUID.randomUUID().toString();
	}

	private void createProcessPID(Message correlatedMessage, String endpoint, String pid) throws UnsupportedEncodingException {
		String processEndpoint = configuration.getClearingHouseUrl() + endpoint + pid;

		RequestMessage processMessage = new RequestMessageBuilder()
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._issuerConnector_(whoIAm())
				._issued_(DateUtil.now())
				._senderAgent_(correlatedMessage.getSenderAgent())
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				.build();


		String fingerprint = getFingerprint(correlatedMessage);

		List<String> owners = new ArrayList<>();
		owners.add(fingerprint);

		MultipartMessage multipartMessage = new MultipartMessageBuilder().withHeaderContent(processMessage)
																		 .withPayloadContent(ownersList(owners))
																		 .build();


		sendDataToBusinessLogicService.sendMessageFormData(processEndpoint, multipartMessage, new HashMap<>());
	}

	private static String ownersList(List<String> list) {
		Map<String, List<String>> owners = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		owners.put("owners", list);

		try {
			return mapper.writeValueAsString(owners);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error to write owners' JSON" + e);
		}
	}

	private static String getFingerprint(Message correlatedMessage) {
		String jwt = correlatedMessage.getSecurityToken().getTokenValue();
		String[] chunks = jwt.split("\\.");

		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(chunks[1]));

		ObjectMapper mapper = new ObjectMapper();

		JsonNode jsonNode;
		try {
			jsonNode = mapper.readTree(payload);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error to read jwt: " + e);
		}
		return jsonNode.get("sub").asText();
	}

	private String extractPIDfromContract(Message correlatedMessage) {
		Pattern uuidPattern = Pattern.compile("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}");
		String path = correlatedMessage.getTransferContract().getPath();
		Matcher matcher = uuidPattern.matcher(path);

		List<String> matches = new ArrayList<>();
		while (matcher.find()) {
			matches.add(matcher.group(0));
		}

		int match = matches.size() - 1;

		//If a UUID cannot be extracted from the contract, a new one is created
		if (match < 0) {
			return createPID();
		}
		return matches.get(matches.size() - 1);
	}

	@NotNull
	private static NotificationContent createNotificationContent(LogMessage logInfo, Message correlatedMessage, String hash) {
		NotificationContent notificationContent = new NotificationContent();

		// Header Management
		notificationContent.setHeader(logInfo);
		//Fix setHeader (notificationContent.setHeader always return "DummyTokenValue")
		notificationContent.getHeader().setSecurityToken(logInfo.getSecurityToken());

		//Body Management
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

