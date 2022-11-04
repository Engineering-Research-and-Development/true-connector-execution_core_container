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
	public String createProcessIdAtClearingHouse(Message contractAgreementHeader, String contractAgreementBody) {
		String providerFingerprint = dapsUtilityProvider.getConnectorUUID();
		String consumerFingerprint = getConsumerFingerprint(contractAgreementHeader);
		if (consumerFingerprint == null || providerFingerprint.isEmpty()) {
			logger.warn("Connector fingerprint missing - Cannot create process at Clearing House\n" + "Provider Fingerprint: {}\n" + "Consumer Fingerprint: {}", providerFingerprint, consumerFingerprint);
			//TODO error? return?
		}
/*
		//remove
//		String pid = extractPIDfromContract(contractAgreementHeader);
*/

		try {
			Response response = createPID(contractAgreementHeader, contractAgreementBody, providerFingerprint, consumerFingerprint);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}


		//TODO return PID
		return "pippo";
	}

	private Response createPID(Message contractAgreement, String contractAgreementBody, String providerFingerprint, String consumerFingerprint) throws UnsupportedEncodingException {
		String processEndpoint = "process/";
/*		// wrong
//		URI messageID = contractAgreement.getId();
		// new one*/
		URI messageID = getMessageId(contractAgreementBody);

		String pid = uuidFromURI(messageID);

		String endpoint = configuration.getClearingHouseUrl() + processEndpoint + pid;

		//TODO send message to create a process at CH
		RequestMessage processMessage = buildRequestMessage(contractAgreement);

		List<String> owners = ownersListOf(providerFingerprint, consumerFingerprint);

		MultipartMessage multipartMessage = buildMultipartMessage(processMessage, owners);

		logger.info("\n\nTry to create PID: {}\n\n", pid);
		Response response = sendDataToBusinessLogicService.sendMessageFormData(endpoint, multipartMessage, new HashMap<>());
		int code = response.code();
		if (code != 201) {
			String message = response.message();
			logger.error("Clearing House didn't create a new log ProcessID - RejectionReason: {} {}\n{}", code, message, response.body());
		} else {
			logger.info("Clearing House created a new log ProcessID: {}", pid);
		}
		return response;
	}

	private static MultipartMessage buildMultipartMessage(RequestMessage processMessage, List<String> owners) {
		Map<String, String> payloadHeader = new HashMap<>();
		payloadHeader.put("content-type", "application/json");
		return new MultipartMessageBuilder().withHeaderContent(processMessage)
											.withPayloadContent(ownersList(owners))
											.withPayloadHeader(payloadHeader)
											.build();
	}

	@NotNull
	private static List<String> ownersListOf(String providerFingerprint, String consumerFingerprint) {
		List<String> owners = new ArrayList<>();
		owners.add(providerFingerprint);
		owners.add(consumerFingerprint);
		return owners;
	}

	private RequestMessage buildRequestMessage(Message contractAgreement) {
		return new RequestMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
										  ._issuerConnector_(whoIAm())
										  ._issued_(DateUtil.now())
										  ._senderAgent_(contractAgreement.getSenderAgent())
										  ._securityToken_(dapsProvider.getDynamicAtributeToken())
										  .build();
	}

	@Override
	public boolean registerTransaction(Message correlatedMessage, String payload) {
		String messageLogEndpoint = "messages/log/";
		/*//		String processEndpoint = "process/";
		 * */

		boolean success = false;
		try {
			logger.info("registerTransaction...");
			String endpoint;
			String pid;

/*//			//TODO check correlatedMessage instanceof ContractAgreementMessage
//			if (correlatedMessage instanceof ContractAgreementMessage) {
//				URI id = getMessageId(payload);
//
//				pid = uuidFromURI(id);
//				if (pid.isEmpty()) {
//					pid = getUuidPid();
//				}
//				createProcessPID(correlatedMessage, processEndpoint, pid);
//			}
//			//TODO Stop*/

			if (correlatedMessage.getTransferContract() != null) {
				//log all exchanged messages relating to same ContractAgreement in same process
				pid = extractPIDfromContract(correlatedMessage);
				/*//				createProcessPID(correlatedMessage, processEndpoint, pid);
				 */
			} else {
				//default random PID
				pid = getUuidPid();
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
			//TODO refactor message
			String chMessageSerialized = serializer.serialize(chMessage);
			logger.info("Serialized Message which is sending to CH=\n{}", chMessageSerialized);
			String sendingDataInfo = "Sending Data to the Clearing House " + endpoint + " ...";
			logger.info(sendingDataInfo);
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

	private URI getMessageId(String message) {
		JsonNode jsonNode = null;
		try {
			jsonNode = new ObjectMapper().readValue(message, JsonNode.class);
		} catch (JsonProcessingException e) {
			logger.error("Cannot serialize JSON: {}\n{}", message, e);

		}
		return jsonNode == null ? URI.create("") : URI.create(jsonNode.get("@id").asText());
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

	private String getUuidPid() {
		return UUID.randomUUID().toString();
	}

	/*private void createProcessPID(Message correlatedMessage, String endpoint, String pid) throws UnsupportedEncodingException {
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

		Response response = sendDataToBusinessLogicService.sendMessageFormData(processEndpoint, multipartMessage, new HashMap<>());
		int code = response.code();
		if (code != 201) {
			String message = response.message();
			logger.warn("Clearing House didn't create a new log ProcessID - RejectionReason: {} {}", code, message);
		} else {
			logger.info("Clearing House created a new log ProcessID: {}", pid);
		}
	}*/

	//refactoring
	private String extractPIDfromContract(Message message) {
		Pattern uuidPattern = Pattern.compile("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}");
		String path = message.getTransferContract().getPath();
		Matcher matcher = uuidPattern.matcher(path);

		List<String> matches = new ArrayList<>();
		while (matcher.find()) {
			matches.add(matcher.group(0));
		}

		int match = matches.size() - 1;

		//If a UUID cannot be extracted from the contract, a new one is created
		if (match < 0) {
			return getUuidPid();
		}
		return matches.get(matches.size() - 1);
	}

	private String uuidFromURI(URI id) {
		Pattern uuidPattern = Pattern.compile("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}");
		Matcher matcher = uuidPattern.matcher(id.getPath());

		List<String> matches = new ArrayList<>();
		while (matcher.find()) {
			matches.add(matcher.group(0));
		}
		return !matches.isEmpty() ? matches.get(matches.size() - 1) : "";
	}

	/*private static String getFingerprint(Message correlatedMessage) {
		String fingerprint = null;
		String token = correlatedMessage.getSecurityToken().getTokenValue();

		try {
			DecodedJWT jwt = new JWT().decodeJwt(token);
			fingerprint = jwt.getSubject();

		} catch (JWTDecodeException e) {
			logger.warn("{}\nToken value: {}", e.getMessage(), token);
		}
		return fingerprint;
	}*/


	private String getConsumerFingerprint(Message contractAgreement) {
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

}

