package it.eng.idsa.businesslogic.audit;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.camel.Message;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.http.HttpMethod;

import it.eng.idsa.multipart.domain.MultipartMessage;

public class TrueConnectorEvent extends AuditApplicationEvent {
	
	private static String correlationId;
	
	// TODO check if this is correct approach
	public String getCorrelationId() {
		return TrueConnectorEvent.correlationId;
	}

	private static final long serialVersionUID = -87655024649097585L;

	public TrueConnectorEvent(String principal, TrueConnectorEventType type, MultipartMessage multipartMessage) {
		super(principal, type.name(), detailsMultipartMessage(multipartMessage));
	}
	
	public TrueConnectorEvent(String principal, TrueConnectorEventType type, Message camelMessage) {
		super(principal, type.name(), detailsCamelMessage(camelMessage));
	}
	
	public TrueConnectorEvent(String principal, TrueConnectorEventType type, Map<String, Object> data) {
		super(principal, type.name(), data);
	}
	
	public TrueConnectorEvent(HttpServletRequest request, TrueConnectorEventType type) {
		super(principal(request), type.name(), details(request));
	}

	private static String principal(HttpServletRequest request) {
		return Optional.ofNullable(request.getUserPrincipal()).map(Principal::getName).orElse("anonymousUser");
	}
	
	private static Map<String, Object> detailsMultipartMessage(MultipartMessage multipartMessage) {
		Map<String, Object> details = new HashMap<>();
		details.put("http.method", HttpMethod.POST);
		details.put("http.message", multipartMessage.getHeaderContent().getClass().getCanonicalName());
//		details.put("http.headers", getHeadersInfo(multipartMessage.getHttpHeaders()));
		return details;
	}
	
	private static Map<String, Object> detailsCamelMessage(Message camelMessage) {
		correlationId = (String) camelMessage.getHeader("correlationId");
		Map<String, Object> details = new HashMap<>();
		details.put("http.method", HttpMethod.POST);
		details.put("http.message", camelMessage.getBody(MultipartMessage.class).getHeaderContent().getClass().getCanonicalName());
//		details.put("http.correlationId", camelMessage.getHeader("correlationId"));
		
		return details;
	}

	private static Map<String, Object> details(HttpServletRequest request) {
		Map<String, Object> details = new HashMap<>();
		details.put("http.method", request.getMethod());
		details.put("http.path", request.getRequestURL());
		details.put("http.headers", getHeadersInfo(request));
		return details;
	}

	private static Map<String, String> getHeadersInfo(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			if(key.equalsIgnoreCase("Authorization")) {
				value = "******";
			}
			map.put(key, value);
		}
		return map;
	}
}
