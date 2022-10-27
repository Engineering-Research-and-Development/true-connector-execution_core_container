package it.eng.idsa.businesslogic.audit;

import java.lang.reflect.Method;

import org.apache.camel.Exchange;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.util.TrueConnectorConstants;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Aspect
@Component
public class CamelProcessorAspect {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Pointcut("@annotation(it.eng.idsa.businesslogic.audit.CamelAuditable)")
	public void loggableMethods() {
	}

	@Pointcut("within(it.eng.idsa.businesslogic.processor..*)")
	public void camelProcessor() {
	}

	@Pointcut("camelProcessor() && loggableMethods()")
	public void auditableProcessor() {
	}
	
	@Before("auditableProcessor()")
    public void logMethodCall(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		CamelAuditable camelAuditable = method.getAnnotation(CamelAuditable.class);
		TrueConnectorEventType beforeEvent = camelAuditable.beforeEventType();
		if(!beforeEvent.equals(TrueConnectorEventType.TRUE_CONNECTOR_EVENT)) {
			MultipartMessage multipartMessage = getMultipartMessage(joinPoint);
			publisher.publishEvent(new TrueConnectorEvent(beforeEvent, multipartMessage, getCorrelationId(joinPoint)));
		}
    }

	@AfterReturning("auditableProcessor()")
	public void logAfterAllMethods(JoinPoint joinPoint) {
		TrueConnectorEventType successEvent = getEventType(joinPoint, true);
		MultipartMessage multipartMessage = getMultipartMessage(joinPoint);
		publisher.publishEvent(new TrueConnectorEvent(successEvent, multipartMessage, getCorrelationId(joinPoint)));
	}

	@AfterThrowing(pointcut = "auditableProcessor()", throwing = "e")
	public void logExceptions(JoinPoint joinPoint, Exception e) {
		TrueConnectorEventType failurEvent = getEventType(joinPoint, false);
		MultipartMessage multipartMessage = getMultipartMessage(joinPoint);
		publisher.publishEvent(new TrueConnectorEvent(failurEvent, multipartMessage, getCorrelationId(joinPoint)));
	}

	private TrueConnectorEventType getEventType(JoinPoint joinPoint, boolean success) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		CamelAuditable camelAuditable = method.getAnnotation(CamelAuditable.class);
		if (success) {
			return camelAuditable.successEventType();
		}
		return camelAuditable.failureEventType();
	}

	private MultipartMessage getMultipartMessage(JoinPoint joinPoint) {
		Object[] signatureArgs = joinPoint.getArgs();
		if (signatureArgs != null && signatureArgs.length == 1) {
			Exchange exchange = (Exchange) signatureArgs[0];
			return exchange.getMessage().getBody(MultipartMessage.class);
		}
		return null;
	}
	
	private String getCorrelationId(JoinPoint joinPoint) {
		Object[] signatureArgs = joinPoint.getArgs();
		if (signatureArgs != null && signatureArgs.length == 1) {
			Exchange exchange = (Exchange) signatureArgs[0];
			return (String) exchange.getMessage().getHeader(TrueConnectorConstants.CORRELATION_ID);
		}
		return null;
	}
}
