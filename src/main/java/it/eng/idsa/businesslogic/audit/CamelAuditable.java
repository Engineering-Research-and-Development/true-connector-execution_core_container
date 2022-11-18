package it.eng.idsa.businesslogic.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CamelAuditable {

	TrueConnectorEventType beforeEventType() default TrueConnectorEventType.TRUE_CONNECTOR_EVENT;
	TrueConnectorEventType successEventType();
	TrueConnectorEventType failureEventType() default TrueConnectorEventType.TRUE_CONNECTOR_EVENT;
}
