package it.eng.idsa.businesslogic.audit;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.http.HttpMethod;

import it.eng.idsa.multipart.domain.MultipartMessage;

public class TrueConnectorEvent extends AuditApplicationEvent {

	private static final long serialVersionUID = -87655024649097585L;

	private static final String IDS_USER = "idsUser";

	public TrueConnectorEvent(String principal, TrueConnectorEventType type, MultipartMessage multipartMessage) {
		super(principal, type.name(), detailsMultipartMessage(multipartMessage, null));
	}

	/**
	 * Uses default principal - connector
	 * 
	 * @param type             TrueConnectorEventType
	 * @param multipartMessage - logs information from multipartMessage to event
	 */
	public TrueConnectorEvent(TrueConnectorEventType type, MultipartMessage multipartMessage) {
		super(IDS_USER, type.name(), detailsMultipartMessage(multipartMessage, null));
	}

	/**
	 * TrueConnector event with correlationId
	 * 
	 * @param type             TrueConnectorEventType
	 * @param multipartMessage - logs information from multipartMessage to event
	 * @param correlationId    correlation Id
	 */
	public TrueConnectorEvent(TrueConnectorEventType type, MultipartMessage multipartMessage, String correlationId) {
		super(IDS_USER, type.name(), detailsMultipartMessage(multipartMessage, correlationId));
	}

	/**
	 * Default TrueConnector event
	 * 
	 * @param principal Principal of the user
	 * @param type      TrueConnectorEventType
	 * @param data      Data for logging
	 */
	public TrueConnectorEvent(String principal, TrueConnectorEventType type, Map<String, Object> data) {
		super(principal, type.name(), data);
	}

	/**
	 * TrueConnectorEvent with request and type\n
	 * 
	 * @param request   Http Request
	 * @param principal Principal of the user
	 * @param type      TrueConnectorEventType
	 */
	public TrueConnectorEvent(HttpServletRequest request, String principal, TrueConnectorEventType type) {
		super(principal, type.name(), details(request, null, null));
	}

	/**
	 * TrueConnectorEvent with request and type\n
	 * 
	 * @param request Http Request
	 * @param type    TrueConnectorEventType
	 */
	public TrueConnectorEvent(HttpServletRequest request, TrueConnectorEventType type) {
		super(principal(request), type.name(), details(request, null, null));
	}

	/**
	 * TrueConnectorEvent with request, type and correlationId
	 * 
	 * @param request       Http Request
	 * @param type          TrueConnectorEventType
	 * @param correlationId correlation id
	 */
	public TrueConnectorEvent(HttpServletRequest request, TrueConnectorEventType type, String correlationId) {
		super(principal(request), type.name(), details(request, correlationId, null));
	}

	/**
	 * TrueConnectorEvent with request, type, correlationId and payload
	 * 
	 * @param request       HTTP request
	 * @param type          TrueConnectorEventType
	 * @param correlationId correlationId for tracking request
	 * @param payload       payload of the request
	 */
	public TrueConnectorEvent(HttpServletRequest request, TrueConnectorEventType type, String correlationId,
			String payload) {
		super(principal(request), type.name(), details(request, correlationId, payload));
	}

	private static String principal(HttpServletRequest request) {
		return Optional.ofNullable(request.getUserPrincipal()).map(Principal::getName).orElse("anonymousUser");
	}

	private static Map<String, Object> detailsMultipartMessage(MultipartMessage multipartMessage,
			String correlationId) {
		Map<String, Object> details = new HashMap<>();
		details.put("http.method", HttpMethod.POST);
		if (correlationId != null) {
			details.put("correlationId", correlationId);
		}
		if (multipartMessage != null) {
			details.put("http.message", multipartMessage.getHeaderContent().getClass().getCanonicalName());
		} else {
			details.put("http.message", "NO MESSAGE");
		}
		return details;
	}

	private static Map<String, Object> details(HttpServletRequest request, String correlationId, String payload) {
		Map<String, Object> details = new HashMap<>();
		details.put("http.method", request.getMethod());
		details.put("http.path", request.getRequestURL());
		details.put("http.headers", getHeadersInfo(request));
		if (correlationId != null) {
			details.put("correlationId", correlationId);
		}

		if (payload != null) {
			details.put("payload", payload);
		}
		return details;
	}

	private static Map<String, String> getHeadersInfo(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			if (key.equalsIgnoreCase("Authorization")) {
				value = "******";
			}
			map.put(key, value);
		}
		return map;
	}
}
