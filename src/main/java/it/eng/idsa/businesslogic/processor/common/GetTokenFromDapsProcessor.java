package it.eng.idsa.businesslogic.processor.common;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
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
	
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("Enriching message with DAT token");

		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		Map<String, Object> headersParts = exchange.getMessage().getHeaders();
		Message message = multipartMessage.getHeaderContent();

		// Get the Token from the DAPS
		String token = null;
		try {
			token = dapsTokenProviderService.provideToken();
			logger.debug("DAT token: {}", token);
		} catch (Exception e) {
			logger.error("Can not get the token from the DAPS server ", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN_LOCAL_ISSUES, message);
		}

		if (token == null) {
			logger.error("Can not get the token from the DAPS server");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		}

		if (token.isEmpty()) {
			logger.error("The token from the DAPS server is empty");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN_LOCAL_ISSUES, message);
		}

//		logger.info("token=" + token);
//		if (RouterType.HTTP_HEADER.equals(eccHttpSendRouter)) {
//			// TODO move this to SendDataToBussinessLogicServiceImpl
//			multipartMessage = new MultipartMessageBuilder().withHttpHeader(multipartMessage.getHttpHeaders())
//					.withHeaderHeader(multipartMessage.getHeaderHeader())
//					.withHeaderContent(multipartMessage.getHeaderContent())
//					.withPayloadHeader(multipartMessage.getPayloadHeader())
//					.withPayloadContent(multipartMessage.getPayloadContent()).withToken(token).build();
//		} else {
			String messageStringWithToken = multipartMessageService.addToken(message, token);
			logger.debug("messageStringWithToken=\n" + messageStringWithToken);

			multipartMessage = new MultipartMessageBuilder()
					.withHttpHeader(multipartMessage.getHttpHeaders())
					.withHeaderHeader(multipartMessage.getHeaderHeader())
					.withHeaderContent(messageStringWithToken)
					.withPayloadHeader(multipartMessage.getPayloadHeader())
					.withPayloadContent(multipartMessage.getPayloadContent())
					.withToken(token)
					.build();
//		}
		// Return exchange
		exchange.getMessage().setBody(multipartMessage);
		exchange.getMessage().setHeaders(headersParts);

	}

}
