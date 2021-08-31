package it.eng.idsa.businesslogic.service.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import okhttp3.Headers;

@Service
public class HttpHeaderServiceImpl implements HttpHeaderService {

	private static final Logger logger = LoggerFactory.getLogger(HttpHeaderService.class);
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> messageToHeaders(Message message) {
		logger.debug("Converting message to http-headers");
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
		logger.debug("Converting http-headers to message");

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
		
		// handle recipientConnector - List
		List<URI> recipientConnector = new ArrayList<>();
		if(headers.containsKey("IDS-recipientConnector")) {
			if(headers.get("IDS-recipientConnector") instanceof String) {
				recipientConnector.add(URI.create((String) headers.get("IDS-recipientConnector")));
			} else {
				recipientConnector = (List<URI>) headers.get("IDS-recipientConnector");
			}
			headers.remove("IDS-recipientConnector");
		}
		// handle recipientAgent - List
		List<URI> recipientAgent = new ArrayList<>();
		if(headers.containsKey("IDS-recipientAgent")) {
			if(headers.get("IDS-recipientAgent") instanceof String) {
				recipientAgent.add(URI.create((String) headers.get("IDS-recipientAgent")));
			} else {
				recipientAgent = (List<URI>) headers.get("IDS-recipientAgent");
			}
			headers.remove("IDS-recipientAgent");
		}
			
		messageAsHeader = getIDSHeaders(headers);
		
		messageAsHeader.put("ids:securityToken", tokeAsMap);
		messageAsHeader.put("ids:recipientConnector", recipientConnector);
		messageAsHeader.put("ids:recipientAgent", recipientAgent);
		messageAsHeader.remove("IDS-Messagetype");
		messageAsHeader.remove("IDS-Id");
		messageAsHeader.put("@type", type);
		messageAsHeader.put("@id", id);
		
		headers.entrySet().removeIf(entry -> entry.getKey().contains("IDS") || entry.getKey().contains("ids"));

		return mapper.convertValue(messageAsHeader, Message.class);
	}

	public Map<String, Object> getIDSHeaders(Map<String, Object> headers) {
		return headers.entrySet().stream()
				.filter(e -> StringUtils.containsIgnoreCase(e.getKey(), "IDS-"))
				.collect(java.util.stream.Collectors.toMap(
						e -> e.getKey().replace("IDS-", "ids:").replace(e.getKey().substring(4,5), e.getKey().substring(4,5).toLowerCase()),
						e -> e.getValue()));
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

	@Override
	public Map<String, Object> okHttpHeadersToMap(Headers headers) {
		logger.debug("Converting headers to map");
		Map<String, Object> originalHeaders = new HashMap<>();

		for (String name : headers.names()) {
			List<String> value = headers.values(name);
			if (value.size() == 1) {
				originalHeaders.put(name, value.get(0));
			} else {
				originalHeaders.put(name, value);
			}
			originalHeaders.keySet().size();
		}
		return originalHeaders;
	}

}
