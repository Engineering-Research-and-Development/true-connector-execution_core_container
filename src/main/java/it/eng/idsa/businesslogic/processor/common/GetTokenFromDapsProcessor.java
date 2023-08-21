package it.eng.idsa.businesslogic.processor.common;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.TrueConnectorConstants;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class GetTokenFromDapsProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(GetTokenFromDapsProcessor.class);

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Autowired(required = false)
	private DapsTokenProviderService dapsTokenProviderService;
	
	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("Enriching message with DAT token");

		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		Map<String, Object> headersParts = exchange.getMessage().getHeaders();
		Message message = multipartMessage.getHeaderContent();

		String correlationId = (String) headersParts.get(TrueConnectorConstants.CORRELATION_ID);
		// Get the Token from the DAPS
		String token = null;
		try {
			token = dapsTokenProviderService.provideToken();
		} catch (Exception e) {
			logger.error("Can not get the token from the DAPS server ", e);
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE, multipartMessage, correlationId));
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.NOT_AUTHENTICATED);
		}

		if (token == null) {
			logger.error("Can not get the token from the DAPS server");
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE, multipartMessage, correlationId));
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.TEMPORARILY_NOT_AVAILABLE);
		}

		if (token.isEmpty()) {
			logger.error("The token from the DAPS server is empty");
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE, multipartMessage, correlationId));
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.INTERNAL_RECIPIENT_ERROR);
		}

		String messageStringWithToken = multipartMessageService.addToken(message, token);

		multipartMessage = new MultipartMessageBuilder()
				.withHttpHeader(multipartMessage.getHttpHeaders())
				.withHeaderHeader(multipartMessage.getHeaderHeader())
				.withHeaderContent(messageStringWithToken)
				.withPayloadHeader(multipartMessage.getPayloadHeader())
				.withPayloadContent(multipartMessage.getPayloadContent())
				.withToken(token)
				.build();
		
		// Return exchange
		exchange.getMessage().setBody(multipartMessage);
		exchange.getMessage().setHeaders(headersParts);
		if (isEnabledDapsInteraction) {
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS, multipartMessage, correlationId));
		}
	}

}
