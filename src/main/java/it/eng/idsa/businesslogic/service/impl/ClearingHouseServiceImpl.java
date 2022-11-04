/**
 *
 */
package it.eng.idsa.businesslogic.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
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

import java.io.IOException;
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
	@Autowired
	private DapsUtilityProvider dapsUtilityProvider;

	@Override
	public void createProcessIdAtClearingHouse(Message contractAgreementHeader, String contractAgreementBody) {
		String providerFingerprint = dapsUtilityProvider.getConnectorUUID();
		String consumerFingerprint = getConsumerFingerprint(contractAgreementHeader);
		if (consumerFingerprint == null || providerFingerprint.isEmpty()) {
			logger.warn("Connector fingerprint missing - Cannot create process at Clearing House\n" + "Provider Fingerprint: {}\n" + "Consumer Fingerprint: {}", providerFingerprint, consumerFingerprint);
			//TODO error? return?
		}

		try {
			createPID(contractAgreementHeader, contractAgreementBody, providerFingerprint, consumerFingerprint);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean registerTransaction(Message correlatedMessage, String payload) {
		String messageLogEndpoint = "messages/log/";

		boolean success = false;
		try {
			logger.info("registerTransaction...");
			String endpoint;
			String pid = "";

			URI transferContractURI = correlatedMessage.getTransferContract();
			if (transferContractURI != null) {
				pid = uuidFromURI(transferContractURI);
			}
			pid = pid.isEmpty() ? UUID.randomUUID().toString() : pid;

			endpoint = configuration.getClearingHouseUrl() + messageLogEndpoint + pid; //Create Message for Clearing House

			LogMessage logInfo = buildLogMessage(correlatedMessage);
			String hash = hashService.hash(payload);
			NotificationContent notificationContent = createNotificationContent(logInfo, correlatedMessage, hash);

			MultipartMessage multipartMessage = buildMultipartMessage(notificationContent);

			logger.info("Sending Data to the Clearing House {} ...", endpoint);
			Response response = sendDataToBusinessLogicService.sendMessageFormData(endpoint, multipartMessage, new HashMap<>());


			logger.info("Data [LogMessage.id={}] sent to the Clearing House {}", logInfo.getId(), endpoint);

			hashService.recordHash(hash, payload, notificationContent);

			int code = response.code();
			if (code == 201) {
				success = true;
			} else {
				String message = response.message();
				logger.error("Clearing House rejects the transaction - RejectionReason: {} {}", code, message);
			}

		} catch (Exception e) {
			logger.error("Could not register the following message to clearing house", e);
		}

		return success;
	}

	private String getConsumerFingerprint(@NotNull Message contractAgreement) {
		String fingerprint = null;
		String token = contractAgreement.getSecurityToken().getTokenValue();

		try {
			DecodedJWT jwt = new JWT().decodeJwt(token);
			fingerprint = jwt.getSubject();

		} catch (JWTDecodeException e) {
			logger.warn("{}\nToken value: {}", e.getMessage(), token);
		}
		return fingerprint;
	}


	private void createPID(Message contractAgreement, String contractAgreementBody, String providerFingerprint, String consumerFingerprint) throws UnsupportedEncodingException {
		String processEndpoint = "process/";
		URI messageID = getMessageId(contractAgreementBody);
		String pid = uuidFromURI(messageID);

		String endpoint = configuration.getClearingHouseUrl() + processEndpoint + pid;

		RequestMessage processMessage = buildRequestMessage(contractAgreement);
		List<String> owners = ownersListOf(providerFingerprint, consumerFingerprint);
		MultipartMessage multipartMessage = buildMultipartMessage(processMessage, owners);

		Response response = sendDataToBusinessLogicService.sendMessageFormData(endpoint, multipartMessage, new HashMap<>());
		int code = response.code();
		if (code != 201) {
			String message = response.message();
			logger.error("Clearing House didn't create a new log ProcessID - RejectionReason: {} {}\n{}", code, message, response.body());
		} else {
			logger.info("Clearing House created a new log ProcessID: {}", pid);
		}
	}

	@NotNull
	private URI getMessageId(String message) {
		JsonNode jsonNode = null;
		try {
			jsonNode = new ObjectMapper().readValue(message, JsonNode.class);
		} catch (JsonProcessingException e) {
			logger.error("Cannot serialize JSON: {}\n{}", message, e);

		}
		return jsonNode == null ? URI.create("") : URI.create(jsonNode.get("@id").asText());
	}

	private String uuidFromURI(@NotNull URI id) {
		Pattern uuidPattern = Pattern.compile("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}");
		Matcher matcher = uuidPattern.matcher(id.getPath());

		List<String> matches = new ArrayList<>();
		while (matcher.find()) {
			matches.add(matcher.group(0));
		}
		return !matches.isEmpty() ? matches.get(matches.size() - 1) : "";
	}

	private RequestMessage buildRequestMessage(@NotNull Message contractAgreement) {
		return new RequestMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
										  ._issuerConnector_(whoIAm())
										  ._issued_(DateUtil.now())
										  ._senderAgent_(contractAgreement.getSenderAgent())
										  ._securityToken_(dapsProvider.getDynamicAtributeToken())
										  .build();
	}

	private LogMessage buildLogMessage(@NotNull Message correlatedMessage) {
		return new LogMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
									  ._issuerConnector_(whoIAm())
									  ._issued_(DateUtil.now())
									  ._senderAgent_(correlatedMessage.getSenderAgent())
									  ._securityToken_(dapsProvider.getDynamicAtributeToken())
									  .build();
	}

	@NotNull
	private static List<String> ownersListOf(String providerFingerprint, String consumerFingerprint) {
		List<String> owners = new ArrayList<>();
		owners.add(providerFingerprint);
		owners.add(consumerFingerprint);
		return owners;
	}

	private static MultipartMessage buildMultipartMessage(RequestMessage processMessage, List<String> owners) {
		Map<String, String> payloadHeader = new HashMap<>();
		payloadHeader.put("content-type", "application/json");
		return new MultipartMessageBuilder().withHeaderContent(processMessage)
											.withPayloadContent(ownersList(owners))
											.withPayloadHeader(payloadHeader)
											.build();
	}

	private static String ownersList(List<String> list) {
		Map<String, List<String>> owners = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		owners.put("owners", list);

		try {
			return mapper.writeValueAsString(owners);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error to write owners' JSON " + e);
		}
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

	private static MultipartMessage buildMultipartMessage(@NotNull NotificationContent notificationContent) throws IOException {
		Serializer serializer = new Serializer();
		Message notificationContentHeader = notificationContent.getHeader();
		Body notificationContentBody = notificationContent.getBody();

		String msgPayload = serializer.serialize(notificationContentBody);
		MultipartMessage multipartMessage = new MultipartMessageBuilder().withHeaderContent(notificationContentHeader)
																		 .withPayloadContent(msgPayload)
																		 .build();

		LoggerCHMessage chMessage = new LoggerCHMessage(notificationContentHeader, notificationContentBody);
		String chMessageSerialized = serializer.serialize(chMessage);
		logger.info("Serialized Message which is sending to Clearing House=\n{}", chMessageSerialized);
		return multipartMessage;
	}

	private URI whoIAm() {
		return selfDescriptionConfiguration.getConnectorURI();
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

