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
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ValidateTokenProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ValidateTokenProcessor.class);
	
	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;
	
	@Value("${application.isEnabledDapsInteraction}")
    private boolean isEnabledDapsInteraction;
	
	@Autowired
	private DapsService dapsService;
	
	@Autowired
	private MultipartMessageService multipartMessageService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private HttpHeaderService httpHeaderService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		if (!isEnabledDapsInteraction) {
            logger.info("Daps interaction not configured - continued with flow");
            return;
        }
		
		Map<String, Object> headersParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		
		String token = multipartMessage.getToken();
		Message message = multipartMessage.getHeaderContent();
		logger.info("token: {}", token);
		
		// Check is "token" valid
		boolean isTokenValid = dapsService.validateToken(token);
		
		if(isTokenValid==false) {			
			logger.error("Token is invalid");
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_TOKEN, 
					message);
		}
		
		logger.info("is token valid: "+isTokenValid);
//		httpHeaderService.removeTokenHeaders(exchange.getMessage().getHeaders());
		multipartMessage = new MultipartMessageBuilder().withHeaderContent(multipartMessage.getHeaderContent())
				.withHttpHeader(multipartMessage.getHttpHeaders()).withHeaderHeader(multipartMessage.getHeaderHeader())
				.withPayloadContent(multipartMessage.getPayloadContent())
				.withPayloadHeader(multipartMessage.getPayloadHeader()).build();
		exchange.getMessage().setHeaders(headersParts);
		if (RouterType.HTTP_HEADER.equals(eccHttpSendRouter)) {
			exchange.getMessage().setBody(multipartMessage);
		}else {
			// not used
//			multipartMessageParts.put("isTokenValid", isTokenValid);
			exchange.getMessage().setBody(multipartMessage);
		}
	}

}
