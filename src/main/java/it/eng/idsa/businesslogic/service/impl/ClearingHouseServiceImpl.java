/**
 *
 */
package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.RequestMessageBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Credentials;
import okhttp3.Response;

/**
 * @author Milan Karajovic and Gabriele De Luca
 */

@ConditionalOnExpression("'${application.clearinghouse.isEnabledClearingHouse}'=='true' && '${application.isEnabledDapsInteraction}'=='true'")
@Service
public class ClearingHouseServiceImpl implements ClearingHouseService {
	private static final Logger logger = LoggerFactory.getLogger(ClearingHouseServiceImpl.class);

	private ClearingHouseConfiguration configuration;

	private SelfDescriptionConfiguration selfDescriptionConfiguration;

	private DapsTokenProviderService dapsProvider;

	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	
	private Boolean isReceiver;
	
	private Serializer serializer;
	
	public ClearingHouseServiceImpl(ClearingHouseConfiguration configuration,
			SelfDescriptionConfiguration selfDescriptionConfiguration,
			DapsTokenProviderService dapsProvider,
			SendDataToBusinessLogicService sendDataToBusinessLogicService,
			@Value("${application.isReceiver}") Boolean isReceiver) {
		super();
		this.configuration = configuration;
		this.selfDescriptionConfiguration = selfDescriptionConfiguration;
		this.dapsProvider = dapsProvider;
		this.sendDataToBusinessLogicService = sendDataToBusinessLogicService;
		this.isReceiver = isReceiver;
		this.serializer = new Serializer();
	}

	@Override
	public boolean registerTransaction(Message correlatedMessage, String payload, Message originalMessage) {
		boolean success = false;
		Response response = null;
		try {
			logger.info("registerTransaction...");
			String pid = null;
			
			// Create pid for Contract Agreement
			if (correlatedMessage instanceof ContractAgreementMessage && isReceiver) {
				pid = createProcessIdAtClearingHouse(correlatedMessage, payload, originalMessage);
			}
			
			if (correlatedMessage instanceof ContractAgreementMessage && !isReceiver) {
				logger.info("Extracting pid from Contract agreement...");
				ContractAgreement contractAgreement = serializer.deserialize(payload, ContractAgreement.class);
				pid = uuidFromURI(contractAgreement.getId());
			}
			
			if (!(correlatedMessage instanceof ContractAgreementMessage) && correlatedMessage.getTransferContract() != null) {
				logger.info("Extracting pid from message...");
				pid = uuidFromURI(correlatedMessage.getTransferContract());
			}
			
			if (pid == null) {
				logger.error("Could not get a valid pid, logging to clearing house aborted.");
				return success;
			}

			String endpoint = configuration.getBaseUrl() + configuration.getLogEndpoint() + pid;

			MultipartMessage multipartMessage = buildMultipartMessageForLogging(correlatedMessage);
			
			logger.info("Sending Data to the Clearing House {} ...", endpoint);
			response = sendDataToBusinessLogicService.sendMessageFormData(endpoint, multipartMessage, getBasicAuth());
			
			logger.info("Data [LogMessage.id={}] sent to the Clearing House {}", multipartMessage.getHeaderContent().getId(), endpoint);

			int code = response.code();
			if (code == 201) {
				success = true;
			} else {
				String message = response.message();
				logger.error("Clearing House rejects the transaction - RejectionReason: {} {}", code, message);
			}

		} catch (Exception e) {
			logger.error("Could not register the following message to clearing house", e);
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return success;
	}

	private Map<String, Object> getBasicAuth() {
		Map<String, Object> headers = new HashMap<>();
		
		if (StringUtils.isNotBlank(configuration.getUsername()) && StringUtils.isNotBlank(configuration.getPassword())) {
			headers.put("Authorization",
					Credentials.basic(configuration.getUsername(), configuration.getPassword()));
		}
		return headers;
	}

	private MultipartMessage buildMultipartMessageForLogging(Message correlatedMessage) throws IOException {
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(buildLogMessage(correlatedMessage))
				.withPayloadContent(MultipartMessageProcessor.serializeToJsonLD(correlatedMessage))
				.build();
		return multipartMessage;
	}

		private String getFingerprint(String token) {
			String fingerprint = null;

			try {
				DecodedJWT jwt = new JWT().decodeJwt(token);
				fingerprint = jwt.getSubject();

			} catch (JWTDecodeException e) {
				logger.warn("{}\nToken value: {}", e.getMessage(), token);
			}
			return fingerprint;
		}


	private String uuidFromURI(URI id) {
		Pattern uuidPattern = Pattern.compile("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}");
		Matcher matcher = uuidPattern.matcher(id.getPath());

		List<String> matches = new ArrayList<>();
		while (matcher.find()) {
			matches.add(matcher.group(0));
		}
		return !matches.isEmpty() ? matches.get(matches.size() - 1) : null;
	}

	private RequestMessage buildRequestMessage(Message contractAgreement) {
		return new RequestMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
										  ._issuerConnector_(whoIAm())
										  ._issued_(DateUtil.now())
										  ._senderAgent_(whoIAm())
										  ._securityToken_(dapsProvider.getDynamicAtributeToken())
										  .build();
	}

	private LogMessage buildLogMessage(Message correlatedMessage) {
		return new LogMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
									  ._issuerConnector_(whoIAm())
									  ._issued_(DateUtil.now())
									  ._senderAgent_(whoIAm())
									  ._securityToken_(dapsProvider.getDynamicAtributeToken())
									  .build();
	}

	private MultipartMessage buildMultipartMessageForPIDCreation(RequestMessage processMessage, Message contractAgreement, Message originalMessage) {
		Map<String, String> payloadHeader = new HashMap<>();
		payloadHeader.put("content-type", MediaType.APPLICATION_JSON_VALUE);
		return new MultipartMessageBuilder().withHeaderContent(processMessage)
											.withPayloadContent(ownersList(originalMessage))
											.withPayloadHeader(payloadHeader)
											.build();
	}

	private String ownersList(Message originalMessage) {
		String fingerprint = getFingerprint(originalMessage.getSecurityToken().getTokenValue());

		List<String> ownersList = new ArrayList<>();
		ownersList.add(fingerprint);
		ownersList.add(getFingerprint(dapsProvider.getDynamicAtributeToken().getTokenValue()));
		
		Map<String, List<String>> owners = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		owners.put("owners", ownersList);

		try {
			return mapper.writeValueAsString(owners);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error to write owners' JSON" + e);
		}
	}


	private URI whoIAm() {
		return selfDescriptionConfiguration.getConnectorURI();
	}
	
	private String createProcessIdAtClearingHouse(Message contractAgreementMessage, String payload, Message originalMessage) throws IOException {
		logger.info("Contract agreement detected, trying to create new ProcessID...");
		ContractAgreement contractAgreement = serializer.deserialize(payload, ContractAgreement.class);
		String pid = uuidFromURI(contractAgreement.getId());
		if (pid == null) {
			logger.error("Can not retrieve valid UUID from Contract Agreement @id");
			return null;
		}
		String endpoint = configuration.getBaseUrl() + configuration.getProcessEndpoint() + pid;

		RequestMessage processMessage = buildRequestMessage(contractAgreementMessage);
		MultipartMessage multipartMessage = buildMultipartMessageForPIDCreation(processMessage, contractAgreementMessage, originalMessage);

		try (Response response = sendDataToBusinessLogicService.sendMessageFormData(endpoint, multipartMessage,
				getBasicAuth())){
			if (response.code() != 201) {
				logger.error("Clearing House didn't create a new log ProcessID - RejectionReason: {} {}\n{}",
						response.code(), response.message(), response.body().string());
				return null;
			} else {
				logger.info("Clearing House created a new log ProcessID: {}", pid);
				return pid;
			} 
		} 
	}
}

