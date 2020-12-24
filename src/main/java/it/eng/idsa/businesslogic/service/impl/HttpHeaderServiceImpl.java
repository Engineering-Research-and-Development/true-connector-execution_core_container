package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Service
public class HttpHeaderServiceImpl implements HttpHeaderService {

	private static final Logger logger = LogManager.getLogger(HttpHeaderService.class);
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Override
	public String getHeaderMessagePartFromHttpHeadersWithToken(Map<String, Object> headers)
			throws JsonProcessingException {

		Map<String, Object> headerAsMap = getHeaderMessagePartAsMap(headers);
		Map<String, Object> tokenAsMap = addTokenHeadersToReceivedMessageHeaders(headers);

		headerAsMap.put("securityToken", tokenAsMap);
		removeTokenHeaders(headers);
		return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(headerAsMap);
	}

	private Map<String, Object> addTokenHeadersToReceivedMessageHeaders(Map<String, Object> headers) {
		Map<String, Object> tokenAsMap = new HashMap<>();
		Map<String, Object> tokenFormatAsMap = new HashMap<>();
		tokenAsMap.put("@type", headers.get("IDS-SecurityToken-Type"));
		tokenAsMap.put("@id", headers.get("IDS-SecurityToken-Id"));
		tokenFormatAsMap.put("@id", headers.get("IDS-SecurityToken-TokenFormat"));
		tokenAsMap.put("tokenFormat", tokenFormatAsMap);
		tokenAsMap.put("tokenValue", headers.get("IDS-SecurityToken-TokenValue"));
		return tokenAsMap;
	}

	@Override
	public void removeTokenHeaders(Map<String, Object> headers) {
		headers.remove("IDS-SecurityToken-Type");
		headers.remove("IDS-SecurityToken-Id");
		headers.remove("IDS-SecurityToken-TokenFormat");
		headers.remove("IDS-SecurityToken-TokenValue");
	}

	@Override
	public Map<String, Object> prepareMessageForSendingAsHttpHeadersWithToken(String header)
			throws JsonParseException, JsonMappingException, IOException {

		Map<String, Object> messageAsMap = prepareMessageForSendingAsHttpHeadersWithoutToken(header);
		addTokenToPreparedMessage(header, messageAsMap);
		return messageAsMap;
	}

	private void addTokenToPreparedMessage(String header, Map<String, Object> messageAsMap) throws IOException {
		Map<String, Object> messageAsMapWithToken = new ObjectMapper().readValue(header, Map.class);

		Map<String, Object> tokenAsMap = (Map<String, Object>) messageAsMapWithToken.get("securityToken");
		messageAsMap.put("IDS-SecurityToken-Type", tokenAsMap.get("@type").toString());
		messageAsMap.put("IDS-SecurityToken-Id", tokenAsMap.get("@id").toString());
		messageAsMap.put("IDS-SecurityToken-TokenValue", tokenAsMap.get("tokenValue").toString());
		Map<String, Object> tokenFormatAsMap = (Map<String, Object>) tokenAsMap.get("tokenFormat");
		messageAsMap.put("IDS-SecurityToken-TokenFormat", tokenFormatAsMap.get("@id").toString());
	}

	@Override
	public String getHeaderMessagePartFromHttpHeadersWithoutToken(Map<String, Object> headers)
			throws JsonProcessingException {

		Map<String, Object> headerAsMap = getHeaderMessagePartAsMap(headers);
//		removeMessageHeadersWithoutToken(headers);
		return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(headerAsMap);
	}

	@Override
	public Map<String, Object> getHeaderMessagePartAsMap(Map<String, Object> headers) {
		Map<String, Object> headerAsMap = new HashMap<>();

		if (headers.get("IDS-Messagetype") != null) {
			headerAsMap.put("@type", headers.get("IDS-Messagetype"));
		}
		if (headers.get("IDS-Id") != null) {
			headerAsMap.put("@id", headers.get("IDS-Id"));
		}
		if (headers.get("IDS-Issued") != null) {
			headerAsMap.put("issued", headers.get("IDS-Issued"));
		}
		if (headers.get("IDS-ModelVersion") != null) {
			headerAsMap.put("modelVersion", headers.get("IDS-ModelVersion"));
		}
		if (headers.get("IDS-IssuerConnector") != null) {
			headerAsMap.put("issuerConnector", headers.get("IDS-IssuerConnector"));
		}
		if (headers.get("IDS-TransferContract") != null) {
			headerAsMap.put("transferContract", headers.get("IDS-TransferContract"));
		}
		if (headers.get("IDS-CorrelationMessage") != null) {
			headerAsMap.put("correlationMessage", headers.get("IDS-CorrelationMessage"));
		}
		if (headers.get("IDS-RequestedArtifact") != null) {
			headerAsMap.put("requestedArtifact", headers.get("IDS-RequestedArtifact"));
		}
		return headerAsMap;
	}

	public void removeMessageHeadersWithoutToken(Map<String, Object> headers) {
		headers.remove("IDS-Messagetype");
		headers.remove("IDS-Id");
		headers.remove("IDS-Issued");
		headers.remove("IDS-ModelVersion");
		headers.remove("IDS-IssuerConnector");
		headers.remove("IDS-TransferContract");
		headers.remove("IDS-CorrelationMessage");
		headers.remove("IDS-RequestedArtifact");

	}

	@Override
	public Map<String, Object> prepareMessageForSendingAsHttpHeadersWithoutToken(String header) throws IOException {
		ObjectMapper oMapper = new ObjectMapper();
		Map<String, Object> messageAsMap = null;
		messageAsMap = oMapper.readValue(header, new TypeReference<Map<String, Object>>() {
		});

		Map<String, Object> headers = new HashMap<>();

		if (messageAsMap.get("@type") != null) {
			headers.put("IDS-Messagetype", (String) messageAsMap.get("@type"));
		}
		if (messageAsMap.get("@id") != null) {
			headers.put("IDS-Id", messageAsMap.get("@id"));
		}
		if (messageAsMap.get("issued") != null) {
			headers.put("IDS-Issued", messageAsMap.get("issued"));
		}
		if (messageAsMap.get("modelVersion") != null) {
			headers.put("IDS-ModelVersion", messageAsMap.get("modelVersion"));
		}
		if (messageAsMap.get("issuerConnector") != null) {
			headers.put("IDS-IssuerConnector", messageAsMap.get("issuerConnector"));
		}
		if (messageAsMap.get("transferContract") != null) {
			headers.put("IDS-TransferContract", messageAsMap.get("transferContract"));
		}
		if (messageAsMap.get("correlationMessage") != null) {
			headers.put("IDS-CorrelationMessage", messageAsMap.get("correlationMessage"));
		}
		if (messageAsMap.get("requestedArtifact") != null) {
			headers.put("IDS-RequestedArtifact", messageAsMap.get("requestedArtifact"));
		}
		return headers;
	}

	@Override
	public Map<String, Object> getHeaderContentHeaders(Map<String, Object> headersParts) {

		Map<String, Object> headerContentHeaders = new HashMap<>();

		if (headersParts.get("IDS-Messagetype") != null) {
			headerContentHeaders.put("IDS-Messagetype", headersParts.get("IDS-Messagetype"));
		}
		if (headersParts.get("IDS-Messagetype") != null) {
			headerContentHeaders.put("IDS-Id", headersParts.get("IDS-Id"));
		}
		if (headersParts.get("IDS-Issued") != null) {
			headerContentHeaders.put("IDS-Issued", headersParts.get("IDS-Issued"));
		}
		if (headersParts.get("IDS-ModelVersion") != null) {
			headerContentHeaders.put("IDS-ModelVersion", headersParts.get("IDS-ModelVersion"));
		}
		if (headersParts.get("IDS-IssuerConnector") != null) {
			headerContentHeaders.put("IDS-IssuerConnector", headersParts.get("IDS-IssuerConnector"));
		}
		if (headersParts.get("IDS-TransferContract") != null) {
			headerContentHeaders.put("IDS-TransferContract", headersParts.get("IDS-TransferContract"));
		}
		if (headersParts.get("IDS-CorrelationMessage") != null) {
			headerContentHeaders.put("IDS-CorrelationMessage", headersParts.get("IDS-CorrelationMessage"));
		}
		if (headersParts.get("IDS-RequestedArtifact") != null) {
			headerContentHeaders.put("IDS-RequestedArtifact", headersParts.get("IDS-RequestedArtifact"));
		}

		if (isEnabledDapsInteraction && headersParts.get("IDS-SecurityToken-TokenValue") != null) {
			headerContentHeaders.put("IDS-SecurityToken-Type", headersParts.get("IDS-SecurityToken-Type"));
			headerContentHeaders.put("IDS-SecurityToken-Id", headersParts.get("IDS-SecurityToken-Id"));
			headerContentHeaders.put("IDS-SecurityToken-TokenFormat",
					headersParts.get("IDS-SecurityToken-TokenFormat"));
			headerContentHeaders.put("IDS-SecurityToken-TokenValue", headersParts.get("IDS-SecurityToken-TokenValue"));
		}

		return headerContentHeaders;
	}

	@Override
	public Map<String, Object> prepareMessageForSendingAsHttpHeaders(MultipartMessage multipartMessage)
			throws IOException {
		ObjectMapper oMapper = new ObjectMapper();
		Map<String, Object> messageAsMap = null;
		messageAsMap = oMapper.readValue(multipartMessage.getHeaderContentString(),
				new TypeReference<Map<String, Object>>() {
				});

		Map<String, Object> headers = new HashMap<>();

		if (messageAsMap.get("@type") != null) {
			headers.put("IDS-Messagetype", (String) messageAsMap.get("@type"));
		}
		if (messageAsMap.get("@id") != null) {
			headers.put("IDS-Id", messageAsMap.get("@id"));
		}
		if (messageAsMap.get("ids:issued") != null) {
			if(messageAsMap.get("ids:issued") instanceof Map) {
				headers.put("IDS-Issued", ((Map)messageAsMap.get("ids:issued")).get("@value"));
			} else {
				headers.put("IDS-Issued", messageAsMap.get("ids:issued"));
			}
		}
		if (messageAsMap.get("ids:modelVersion") != null) {
			headers.put("IDS-ModelVersion", messageAsMap.get("ids:modelVersion"));
		}
		if (messageAsMap.get("ids:issuerConnector") != null) {
			if(messageAsMap.get("ids:issuerConnector") instanceof Map) {
				headers.put("IDS-IssuerConnector", ((Map)messageAsMap.get("ids:issuerConnector")).get("@id"));
			} else {
				headers.put("IDS-IssuerConnector", messageAsMap.get("ids:issuerConnector"));
			}
		}
		if (messageAsMap.get("ids:transferContract") != null) {
			if(messageAsMap.get("ids:transferContract") instanceof Map) {
				headers.put("IDS-TransferContract", ((Map)messageAsMap.get("ids:transferContract")).get("@id"));
			} else {
				headers.put("IDS-TransferContract", messageAsMap.get("ids:transferContract"));
			}
		}
		if (messageAsMap.get("ids:correlationMessage") != null) {
			if(messageAsMap.get("ids:correlationMessage") instanceof Map) {
				headers.put("IDS-CorrelationMessage", ((Map)messageAsMap.get("ids:correlationMessage")).get("@id"));
			} else {
				headers.put("IDS-CorrelationMessage", messageAsMap.get("ids:correlationMessage"));
			}
		}
		if (messageAsMap.get("ids:requestedArtifact") != null) {
			if(messageAsMap.get("ids:requestedArtifact") instanceof Map) {
				headers.put("IDS-RequestedArtifact", ((Map)messageAsMap.get("ids:requestedArtifact")).get("@id"));
			} else {
				headers.put("IDS-RequestedArtifact", messageAsMap.get("ids:requestedArtifact"));
			}
		}
		if (messageAsMap.get("ids:rejectionReason") != null) {
			Map<String, Object> rejectionReason = (Map<String, Object>) messageAsMap.get("ids:rejectionReason");
			headers.put("IDS-RejectionReason", rejectionReason.get("@id"));
		}
		return headers;
	}

	@Override
	public Map<String, String> convertMapToStringString(Map<String, Object> map) {
		return map.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
	}

	@Override
	public Map<String, Object> transformJWTTokenToHeaders(String token)
			throws JsonProcessingException {
		Map<String, Object> tokenAsMap = new HashMap<>();
		Token tokenJsonValue = new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_(token).build();
		String tokenValueSerialized = new Serializer().serializePlainJson(tokenJsonValue);
		JSONParser parser = new JSONParser();
		JSONObject jsonObjectToken;
		try {
			jsonObjectToken = (JSONObject) parser.parse(tokenValueSerialized);
			tokenAsMap.put("IDS-SecurityToken-Type", jsonObjectToken.get("@type").toString());
			tokenAsMap.put("IDS-SecurityToken-Id", tokenJsonValue.getId().toString());
			tokenAsMap.put("IDS-SecurityToken-TokenFormat", tokenJsonValue.getTokenFormat().toString());
			tokenAsMap.put("IDS-SecurityToken-TokenValue", token);
		} catch (ParseException e) {
			logger.error("Error while trying to convert from String to json", e);
		}
		return tokenAsMap;
	}

}
