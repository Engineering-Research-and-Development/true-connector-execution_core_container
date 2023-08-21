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
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.util.Helper;
import okhttp3.Headers;

@Service
public class HttpHeaderServiceImpl implements HttpHeaderService {

	private static final Logger logger = LoggerFactory.getLogger(HttpHeaderService.class);

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> messageToHeaders(Message message) {
		if (logger.isDebugEnabled()) {
			logger.debug("Converting following message to http-headers: {}", Helper.getIDSMessageType(message));
		}
		
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
			if (entry.getKey().equals("@id")) {
				headers.put("IDS-Id", message.getId().toString());
			} else if (entry.getKey().equals("@type")) {
				// when using Java it looks like this
				// headers.put("IDS-Messagetype", "ids:" +
				// message.getClass().getInterfaces()[1].getSimpleName());
				// for now we agreed to use the following, because of simplicity
				headers.put("IDS-Messagetype", entry.getValue());
			} else if (entry.getKey().equals("ids:securityToken")) {
				headers.put("IDS-SecurityToken-Type", ((Map<String, Object>) entry.getValue()).get("@type"));
				headers.put("IDS-SecurityToken-Id", message.getSecurityToken().getId().toString());
				headers.put("IDS-SecurityToken-TokenFormat", message.getSecurityToken().getTokenFormat().toString());
				headers.put("IDS-SecurityToken-TokenValue", message.getSecurityToken().getTokenValue());
			} else if (entry.getValue() instanceof Map) {
				Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
				if (valueMap.get("@id") != null) {
					headers.put(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), valueMap.get("@id"));
				} else if (valueMap.get("@value") != null) {
					headers.put(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), valueMap.get("@value"));
				}
			} else {
				if (entry.getValue() instanceof List) {
					// handle list
					if (!CollectionUtils.isEmpty((List) entry.getValue())) {
						headers.put(
								entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
										entry.getKey().substring(4, 5).toUpperCase()),
								entry.getValue());
					}
				} else {
					headers.put(entry.getKey().replaceFirst("ids:", "IDS-").replaceFirst(entry.getKey().substring(4, 5),
							entry.getKey().substring(4, 5).toUpperCase()), entry.getValue());
				}
			}
		});

		logger.debug("Message converted");

		return headers;
	}

	@Override
	public Message headersToMessage(Map<String, Object> headers) {
		// bare in mind that in rumtime, headers is
		// org.apache.camel.util.CaseInsensitiveMap
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
		if (headers.containsKey("IDS-SecurityToken-Type")) {
			tokeAsMap = processDAPSTokenHeaders(headers);
		}
		String type = (String) headers.get("IDS-Messagetype");
		String id = (String) headers.get("IDS-Id");

		// handle recipientConnector - List
		if (headers.containsKey("IDS-RecipientConnector")) {
			List<URI> recipientConnector = new ArrayList<>();
			if (headers.get("IDS-RecipientConnector") instanceof String) {
				recipientConnector.add(URI.create((String) headers.get("IDS-RecipientConnector")));
			} else {
				recipientConnector = (List<URI>) headers.get("IDS-RecipientConnector");
			}
			messageAsHeader.put("ids:recipientConnector", recipientConnector);
			headers.remove("IDS-RecipientConnector");
		}
		// handle recipientAgent - List
		if (headers.containsKey("IDS-RecipientAgent")) {
			List<URI> recipientAgent = new ArrayList<>();
			if (headers.get("IDS-RecipientAgent") instanceof String) {
				recipientAgent.add(URI.create((String) headers.get("IDS-RecipientAgent")));
			} else {
				recipientAgent = (List<URI>) headers.get("IDS-RecipientAgent");
			}
			messageAsHeader.put("ids:recipientAgent", recipientAgent);
			headers.remove("IDS-RecipientAgent");
		}

		messageAsHeader = getIDSHeaders(headers);

		messageAsHeader.put("ids:securityToken", tokeAsMap);
		messageAsHeader.remove("ids:messagetype");
		messageAsHeader.remove("ids:id");
		messageAsHeader.put("@type", type);
		messageAsHeader.put("@id", id);

		headers.entrySet().removeIf(entry -> entry.getKey().startsWith("IDS-") || entry.getKey().startsWith("ids:"));

		Message message = mapper.convertValue(messageAsHeader, Message.class);

		if (logger.isDebugEnabled()) {
			logger.debug("Headers converted to: {}", Helper.getIDSMessageType(message));
		}

		return message;
	}

	public Map<String, Object> getIDSHeaders(Map<String, Object> headers) {
		return headers.entrySet().stream().filter(e -> StringUtils.containsIgnoreCase(e.getKey(), "IDS-"))
				.collect(java.util.stream.Collectors.toMap(e -> e.getKey().replaceFirst("IDS-", "ids:")
						.replaceFirst(e.getKey().substring(4, 5), e.getKey().substring(4, 5).toLowerCase()),
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
		logger.debug("Converting okHttpHeaders to map");
		Map<String, Object> originalHeaders = new HashMap<>();

		for (String name : headers.names()) {
			List<String> value = headers.values(name);
			if (name.startsWith("IDS-")) {
				name = name.replaceFirst(name.substring(4, 5), name.substring(4, 5).toUpperCase());
			}
			if (value.size() == 1) {
				originalHeaders.put(name, value.get(0));
			} else {
				originalHeaders.put(name, value);
			}
		}
		logger.debug("OkHttpHeaders converted to map");

		return originalHeaders;
	}

}
