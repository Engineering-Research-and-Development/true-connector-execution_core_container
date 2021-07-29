package it.eng.idsa.businesslogic.service.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;
import it.eng.idsa.businesslogic.service.HttpHeaderService;

@Service
public class HttpHeaderServiceImpl implements HttpHeaderService {

	private static final Logger logger = LoggerFactory.getLogger(HttpHeaderService.class);
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> messageToHeaders(Message message) {
		Map<String, Object> headers = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		// exclude null values from map
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer());
		mapper.registerModule(simpleModule);
		
		Map<String, Object> messageAsMap = mapper.convertValue(message, new TypeReference<Map<String, Object>>() {
		});
		
		messageAsMap.entrySet().forEach(entry -> {
			if(entry.getKey().equals("@id")) {
				headers.put("IDS-Id", entry.getValue());
			} else if(entry.getKey().equals("@type")) {
				headers.put("IDS-Messagetype", entry.getValue());
			} else if (entry.getKey().equals("ids:securityToken")) {
				headers.put("IDS-SecurityToken-Type", ((Map<String, Object>) entry.getValue()).get("@type"));
				headers.put("IDS-SecurityToken-Id", ((Map<String, Object>) entry.getValue()).get("@id"));
				headers.put("IDS-SecurityToken-TokenFormat", ((Map<String, Object>)((Map<String, Object>) entry.getValue()).get("ids:tokenFormat")).get("@id"));
				headers.put("IDS-SecurityToken-TokenValue", ((Map<String, Object>) entry.getValue()).get("ids:tokenValue"));
			} else if(entry.getValue() instanceof Map) {
				Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
				if(valueMap.get("@id") != null) {
					headers.put(entry.getKey().replace("ids:", "IDS-"), valueMap.get("@id"));
				} else if(valueMap.get("@value") != null) {
					headers.put(entry.getKey().replace("ids:", "IDS-"), valueMap.get("@value"));
				}
			} else {
				headers.put(entry.getKey().replace("ids:", "IDS-"), entry.getValue());
			}
		});
		return headers;
	}

	@Override
	public Message headersToMessage(Map<String, Object> headers) {
		// bare in mind that in rumtime, headers is org.apache.camel.util.CaseInsensitiveMap
		// which means that headers.get("aaa") is the same like headers.get("Aaa")
		Map<String, Object> messageAsHeader = new HashMap<>();

		ObjectMapper mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
		mapper.registerModule(simpleModule);

		// exclude null values from map
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		Map<String, Object> tokeAsMap = null;
		if( headers.containsKey("IDS-SecurityToken-Type")) {
			tokeAsMap = processDAPSTokenHeaders(headers);
		}
		String type = (String) headers.get("IDS-Messagetype");
		String id = (String) headers.get("IDS-Id");
		
		// handle RecipientConnector - List
		List<URI> recipientConnector = null;
		if(headers.containsKey("IDS-recipientConnector")) {
			String ss = (String) headers.get("IDS-recipientConnector");
			recipientConnector = Stream.of(ss.split(","))
					.map(String::trim)
					.map(URI::create)
					.collect(Collectors.toList());
			headers.remove("IDS-recipientConnector");
		}
		List<URI> recipientAgent = null;
		// handle RecipientAgent - List
		if(headers.containsKey("IDS-recipientagent")) {
			recipientAgent = Stream.of(((String) headers.get("IDS-recipientAgent")).split(","))
					.map(String::trim)
					.map(URI::create)
					.collect(Collectors.toList());
			headers.remove("IDS-recipientagent");
		}
			
		messageAsHeader = headers.entrySet().stream()
				.filter(e -> StringUtils.containsIgnoreCase(e.getKey(), "IDS-"))
				.collect(java.util.stream.Collectors.toMap(
						e -> e.getKey().replace("IDS-", "ids:"), 
						e -> e.getValue()));
		
		messageAsHeader.put("ids:securityToken", tokeAsMap);
		messageAsHeader.put("ids:recipientConnector", recipientConnector);
		messageAsHeader.put("ids:recipientAgent", recipientAgent);
		messageAsHeader.remove("IDS-Messagetype");
		messageAsHeader.remove("IDS-Id");
		messageAsHeader.put("@type", type);
		messageAsHeader.put("@id", id);

		return mapper.convertValue(messageAsHeader, Message.class);
	}
	
	private Map<String, Object> processDAPSTokenHeaders(Map<String, Object> headers) {
		Map<String, Object> tokenAsMap = new HashMap<>();
		Map<String, Object> tokenFormatAsMap = new HashMap<>();
		tokenAsMap.put("@type", headers.get("IDS-SecurityToken-Type"));
		tokenAsMap.put("@id", headers.get("IDS-SecurityToken-Id"));
		tokenFormatAsMap.put("@id", headers.get("IDS-SecurityToken-TokenFormat"));
		tokenAsMap.put("ids:tokenFormat", tokenFormatAsMap);
		tokenAsMap.put("ids:tokenValue", headers.get("IDS-SecurityToken-TokenValue"));
		
		headers.remove("IDS-SecurityToken-Type");
		headers.remove("IDS-SecurityToken-Id");
		headers.remove("IDS-SecurityToken-TokenFormat");
		headers.remove("IDS-SecurityToken-TokenValue");
		return tokenAsMap;
	}
	
	@Override
	public Map<String, String> convertMapToStringString(Map<String, Object> map) {
		return map.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
	}

	/*
	 * OLD Methods, considered for removal
	 */
/*
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
*/

}
