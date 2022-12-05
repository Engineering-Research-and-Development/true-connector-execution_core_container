package it.eng.idsa.businesslogic.processor.common;

import javax.security.auth.Subject;

import org.apache.camel.CamelAuthorizationException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;

@Component
public class TrueConnectorAuthorization implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		 // get the username and password from the HTTP header
		String authorization = exchange.getIn().getHeader("Authorization", String.class);
		//TODO authorization can be null if not sent - handle this usecase
		if(StringUtils.isBlank(authorization)) {
			exchange.setProperty(Exchange.EXCEPTION_CAUGHT, new CamelAuthorizationException("Invalid credentials", exchange));
			throw new ExceptionForProcessor("Invalid credentials");
		} else {
		authorization = authorization.replace("Basic ", "");
        String userpass = new String(Base64.decodeBase64(authorization));
        String[] tokens = userpass.split(":");

        // create an Authentication object
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(tokens[0], tokens[1]);

        // wrap it in a Subject
        Subject subject = new Subject();
        subject.getPrincipals().add(authToken);

        // place the Subject in the In message
        exchange.getIn().setHeader(Exchange.AUTHENTICATION, subject);
		}
	}
}
