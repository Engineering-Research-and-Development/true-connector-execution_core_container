package it.eng.idsa.businesslogic.processor.common;

import javax.security.auth.Subject;

import org.apache.camel.CamelAuthorizationException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TrueConnectorAuthorization implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(TrueConnectorAuthorization.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		 // get the username and password from the HTTP header
		String authorization = exchange.getIn().getHeader("Authorization", String.class);
		if(StringUtils.isBlank(authorization)) {
			logger.info("Authorization header not present!!!");
			throw new CamelAuthorizationException("Invalid credentials", exchange);
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
