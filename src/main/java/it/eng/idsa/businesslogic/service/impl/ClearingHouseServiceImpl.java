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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.RequestMessageBuilder;
import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.util.Helper;
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
	
	private RestTemplate restTemplate;
	
	public ClearingHouseServiceImpl(ClearingHouseConfiguration configuration,
			SelfDescriptionConfiguration selfDescriptionConfiguration,
			DapsTokenProviderService dapsProvider,
			SendDataToBusinessLogicService sendDataToBusinessLogicService,
			RestTemplate restTemplate) {
		super();
		this.configuration = configuration;
		this.selfDescriptionConfiguration = selfDescriptionConfiguration;
		this.dapsProvider = dapsProvider;
		this.sendDataToBusinessLogicService = sendDataToBusinessLogicService;
		this.restTemplate = restTemplate;
	}
	
	@Override
	public String createProcessIdAtClearingHouse(String senderToken, String contractAgreementUUID) {
		logger.info("Contract agreement detected, trying to create new ProcessID...");
		Response response = null;

		if (contractAgreementUUID == null) {
			logger.error("Can not retrieve valid UUID from Contract Agreement @id");
			return null;
		}
		String endpoint = configuration.getBaseUrl() + configuration.getProcessEndpoint() + contractAgreementUUID;

		RequestMessage processMessage = buildRequestMessage();
		MultipartMessage multipartMessage = buildMultipartMessageForPIDCreation(processMessage, senderToken);
		try {
			response = sendDataToBusinessLogicService.sendMessageFormData(endpoint, multipartMessage, getBasicAuth());

			if (response.code() != 201) {
				logger.error("Clearing House didn't create a new log ProcessID - RejectionReason: {} {}\n{}",
						response.code(), response.message(), response.body().string());
				return null;
			} else {
				logger.info("Clearing House created a new log ProcessID: {}", contractAgreementUUID);
				return contractAgreementUUID;
			}
		} catch (Exception e) {
			logger.error("Clearing House didn't create a new log ProcessID - {}", e.getMessage());
			return null;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	@Override
	public boolean registerTransaction(Message messageForLogging, String contractAgreementUUID) {
		boolean success = false;
		Response response = null;
		try {
			logger.info("registerTransaction...");
			String pid = null;
			
			if (messageForLogging instanceof ContractAgreementMessage) {
				logger.info("Extracting pid from Contract agreement...");
				pid = contractAgreementUUID;
			}
			
			if (!(messageForLogging instanceof ContractAgreementMessage) && messageForLogging.getTransferContract() != null) {
				logger.info("Extracting pid from message...");
				pid = Helper.getUUID(messageForLogging.getTransferContract());
			}
			
			if (pid == null) {
				logger.error("Could not get a valid pid, logging to clearing house aborted.");
				return success;
			}

			String endpoint = configuration.getBaseUrl() + configuration.getLogEndpoint() + pid;

			MultipartMessage multipartMessage = buildMultipartMessageForLogging(messageForLogging);
			
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
	
	
	@Override
	public boolean isClearingHouseAvailable(String clearingHouseHealthEndpoint) {
		HttpEntity<String> entity = new HttpEntity<>() {};
		
		if (StringUtils.isNotBlank(configuration.getUsername()) && StringUtils.isNotBlank(configuration.getPassword())) {
			entity.getHeaders().set(HttpHeaders.AUTHORIZATION, Credentials.basic(configuration.getUsername(), configuration.getPassword()));
		}
		
		try {
			restTemplate.exchange(clearingHouseHealthEndpoint, HttpMethod.GET, entity, String.class);
		} catch (Exception e) {
			logger.error("Error while making a request", e);
			return false;
		}
		return true;
	}

	private Map<String, Object> getBasicAuth() {
		Map<String, Object> map = new HashMap<>();
		if (StringUtils.isNotBlank(configuration.getUsername()) && StringUtils.isNotBlank(configuration.getPassword())) {
			map.put(HttpHeaders.AUTHORIZATION, Credentials.basic(configuration.getUsername(), configuration.getPassword()));
		}
		return map;
	}

	private MultipartMessage buildMultipartMessageForLogging(Message messageForLogging) throws IOException {
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(buildLogMessage(messageForLogging))
				.withPayloadContent(MultipartMessageProcessor.serializeToJsonLD(messageForLogging))
				.build();
		return multipartMessage;
	}

		private String getFingerprint(String token) {
			String fingerprint = null;

			try {
				DecodedJWT jwt = new JWT().decodeJwt(token);
				fingerprint = jwt.getSubject();

			} catch (JWTDecodeException e) {
				logger.warn("Could not decode jwt: {}", e.getMessage());
			}
			return fingerprint;
		}


	private RequestMessage buildRequestMessage() {
		return new RequestMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
										  ._issuerConnector_(whoIAm())
										  ._issued_(DateUtil.normalizedDateTime())
										  ._senderAgent_(whoIAm())
										  ._securityToken_(dapsProvider.getDynamicAtributeToken())
										  .build();
	}

	private MultipartMessage buildMultipartMessageForPIDCreation(RequestMessage requestMessage, String senderToken) {
		return new MultipartMessageBuilder().withHeaderContent(requestMessage)
				.withPayloadContent(ownersList(senderToken))
				.withPayloadHeader(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
				.build();
	}
	
	private LogMessage buildLogMessage(Message correlatedMessage) {
		return new LogMessageBuilder()._modelVersion_(UtilMessageService.MODEL_VERSION)
									  ._issuerConnector_(whoIAm())
									  ._issued_(DateUtil.normalizedDateTime())
									  ._senderAgent_(whoIAm())
									  ._securityToken_(dapsProvider.getDynamicAtributeToken())
									  .build();
	}


	private String ownersList(String senderToken) {

		List<String> ownersList = new ArrayList<>();
		
		//sender fingerprint
		ownersList.add(getFingerprint(senderToken));
		//receiver fingerprint
		ownersList.add(dapsProvider.getConnectorUUID());
		
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
}

