package it.eng.idsa.businesslogic.audit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import it.eng.idsa.businesslogic.util.TrueConnectorConstants;

@Component
public class TrueConnectorAuditableInterceptor extends HandlerInterceptorAdapter {
	
	private final ApplicationEventPublisher publisher;
	
	public TrueConnectorAuditableInterceptor(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if(handler instanceof HandlerMethod) {
			Auditable annotation = ((HandlerMethod) handler).getMethodAnnotation(Auditable.class);
			if (annotation != null) {
				publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.HTTP_REQUEST_RECEIVED,
						response.getHeader(TrueConnectorConstants.CORRELATION_ID)));
			}
		}
		return super.preHandle(request, response, handler);
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if(handler instanceof HandlerMethod) {
			Auditable annotation = ((HandlerMethod) handler).getMethodAnnotation(Auditable.class);
			if (annotation != null) {
//				fire event for someone to log it or process
				publisher.publishEvent(new TrueConnectorEvent(request, annotation.eventType(), 
						response.getHeader(TrueConnectorConstants.CORRELATION_ID)));
			}
		}
		super.postHandle(request, response, handler, modelAndView);
	}
}
