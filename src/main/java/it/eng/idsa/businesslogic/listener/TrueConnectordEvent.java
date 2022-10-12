package it.eng.idsa.businesslogic.listener;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;

public class TrueConnectordEvent  extends AuditApplicationEvent {

	private static final long serialVersionUID = -87655024649097585L;

	public TrueConnectordEvent(HttpServletRequest request, TrueConnectorEventType type) {
		super(principal(request), type.name(), details(request));
	}

	private static String principal(HttpServletRequest request) {
		return Optional.ofNullable(request.getUserPrincipal()).map(Principal::getName).orElse("anonymousUser");
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
