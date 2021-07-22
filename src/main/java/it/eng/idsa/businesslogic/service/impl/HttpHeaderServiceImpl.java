package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(HttpHeaderService.class);
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	static Map<String, String> contextPart;
	
	static {
		contextPart = new HashMap<>();
		contextPart.put("ids", "https://w3id.org/idsa/core/");
		contextPart.put("idsc",  "https://w3id.org/idsa/code/");
	}

	@Override
	public String getHeaderMessagePartFromHttpHeaders(Map<String, Object> headers)
			throws IOException {

		Map<String, Object> headerAsMap = getHeaderMessagePartAsMap(headers);
		Map<String, Object> tokenAsMap = addTokenHeadersToReceivedMessageHeaders(headers);

		headerAsMap.put("ids:securityToken", tokenAsMap);
		return new Serializer().serialize(headerAsMap);
	}

	private Map<String, Object> addTokenHeadersToReceivedMessageHeaders(Map<String, Object> headers) {
		Map<String, Object> tokenAsMap = new HashMap<>();
		Map<String, Object> tokenFormatAsMap = new HashMap<>();
		tokenAsMap.put("@type", headers.get("IDS-SecurityToken-Type"));
		tokenAsMap.put("@id", headers.get("IDS-SecurityToken-Id"));
		tokenFormatAsMap.put("@id", headers.get("IDS-SecurityToken-TokenFormat"));
		tokenAsMap.put("ids:tokenFormat", tokenFormatAsMap);
		tokenAsMap.put("ids:tokenValue", headers.get("IDS-SecurityToken-TokenValue"));
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

	//here we have all mandatory fields
	@Override
	public Map<String, Object> getHeaderMessagePartAsMap(Map<String, Object> headers) {
		Map<String, Object> headerAsMap = new HashMap<>();

		headerAsMap.put("@context", contextPart);
		if (headers.get("IDS-Messagetype") != null) {
			headerAsMap.put("@type", headers.get("IDS-Messagetype"));
		}
		if (headers.get("IDS-Id") != null) {
			headerAsMap.put("@id", headers.get("IDS-Id"));
		}
		if (headers.get("IDS-Issued") != null) {
			Map<String, String> issued = new HashMap<>();
			issued.put("@value", (String) headers.get("IDS-Issued"));
			issued.put("@type", "http://www.w3.org/2001/XMLSchema#dateTimeStamp");
			headerAsMap.put("ids:issued", issued);
		}
		if (headers.get("IDS-ModelVersion") != null) {
			headerAsMap.put("ids:modelVersion", headers.get("IDS-ModelVersion"));
		}
		if (headers.get("IDS-IssuerConnector") != null) {
			Map<String, String> ic = new HashMap<>();
			ic.put("@id", (String) headers.get("IDS-IssuerConnector"));
			headerAsMap.put("ids:issuerConnector", ic);
		}
		if (headers.get("IDS-TransferContract") != null) {
			headerAsMap.put("ids:transferContract", headers.get("IDS-TransferContract"));
		}
		if (headers.get("IDS-CorrelationMessage") != null) {
			Map<String, String> cm = new HashMap<>();
			cm.put("@id", (String) headers.get("IDS-CorrelationMessage"));
			headerAsMap.put("ids:correlationMessage", cm);
		}
		if (headers.get("IDS-RequestedArtifact") != null) {
			Map<String, String> ra = new HashMap<>();
			ra.put("@id", (String) headers.get("IDS-RequestedArtifact"));
			headerAsMap.put("ids:requestedArtifact", ra);
		}
		if (headers.get("IDS-SenderAgent") != null) {
			Map<String, String> sa = new HashMap<>();
			sa.put("@id", (String)  headers.get("IDS-SenderAgent"));
			headerAsMap.put("ids:senderAgent", sa);
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
		headers.remove("IDS-SenderAgent");
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
		if (messageAsMap.get("senderAgent") != null) {
			headers.put("IDS-SenderAgent", messageAsMap.get("senderAgent"));
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
		if (headersParts.get("IDS-SenderAgent") != null) {
			headerContentHeaders.put("IDS-SenderAgent", headersParts.get("IDS-SenderAgent"));
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
		if (messageAsMap.get("ids:senderAgent") != null) {
			if(messageAsMap.get("ids:senderAgent") instanceof Map) {
				headers.put("IDS-SenderAgent", ((Map)messageAsMap.get("ids:senderAgent")).get("@id"));
			} else {
				headers.put("IDS-SenderAgent", messageAsMap.get("ids:senderAgent"));
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
