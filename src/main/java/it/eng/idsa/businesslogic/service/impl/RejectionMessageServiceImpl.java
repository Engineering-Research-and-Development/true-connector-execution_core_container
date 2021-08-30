package it.eng.idsa.businesslogic.service.impl;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ResultMessageBuilder;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Service
@Transactional
public class RejectionMessageServiceImpl implements RejectionMessageService{
	
	private static final Logger logger = LoggerFactory.getLogger(RejectionMessageServiceImpl.class);
	
	@Autowired
	private DapsTokenProviderService dapsProvider;
	
	@Autowired
	private SelfDescriptionConfiguration selfDescriptionConfiguration;

	@Value("${information.model.version}")
	private String informationModelVersion;

	@Override
	public void sendRejectionMessage(RejectionMessageType rejectionMessageType, Message message) {
		logger.info("Creating rejection message of type {}", rejectionMessageType);
		Message rejectionMessage = createRejectionMessage(rejectionMessageType.toString(), message);

		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(rejectionMessage)
				.build();
		String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);

		throw new ExceptionForProcessor(multipartMessageString);
	}

	private Message createRejectionMessage(String rejectionMessageType, Message message) {
		Message rejectionMessage = null;
		switch(rejectionMessageType) {
			case "RESULT_MESSAGE":
				rejectionMessage = createResultMessage(message);
				break;
			case "REJECTION_MESSAGE_COMMON":
				rejectionMessage = createRejectionMessageCommon(message);
				break;
			case "REJECTION_TOKEN":
				rejectionMessage = createRejectionToken(message);
				break;
			case "REJECTION_MESSAGE_LOCAL_ISSUES":
				rejectionMessage = createRejectionMessageLocalIssues(message);
				break;
			case "REJECTION_TOKEN_LOCAL_ISSUES":
				rejectionMessage = createRejectionTokenLocalIssues(message);
				break;
			case "REJECTION_COMMUNICATION_LOCAL_ISSUES":
				rejectionMessage = createRejectionCommunicationLocalIssues(message);
				break;
			case "REJECTION_USAGE_CONTROL":
				rejectionMessage = createRejectionUsageControl(message);
				break;
			default:
				rejectionMessage = createResultMessage(message);
				break;
		}
		return rejectionMessage;
	}

	public void setInformationModelVersion(String informationModelVersion) {
		this.informationModelVersion = informationModelVersion;
	}

	private Message createResultMessage(Message header) {
		return new ResultMessageBuilder()
				._issuerConnector_(whoIAm())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("http://auto-generated.com")))
				._correlationMessage_(header!=null?header.getId():URI.create("http://auto-generated.com"))
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._senderAgent_(whoIAm())
				.build();
	}

	private Message createRejectionMessageCommon(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAm())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("http://auto-generated.com")))
				._correlationMessage_(header!=null?header.getId():URI.create("http://auto-generated.com"))
				._rejectionReason_(RejectionReason.MALFORMED_MESSAGE)
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._senderAgent_(whoIAm())
				.build();
	}

	private Message createRejectionToken(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAm())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("http://auto-generated.com")))
				._correlationMessage_(header!=null?header.getId():URI.create("http://auto-generated.com"))
				._rejectionReason_(RejectionReason.NOT_AUTHENTICATED)
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._senderAgent_(whoIAm())
				.build();
	}


	private URI whoIAm() {
		return selfDescriptionConfiguration.getConnectorURI();
//		return URI.create("http://auto-generated");
	}

	private Message createRejectionMessageLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(URI.create("http://auto-generated.com"))
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				//._recipientConnectors_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("http://auto-generated.com")))
				._correlationMessage_(header!=null?header.getId():URI.create("http://auto-generated.com"))
				._rejectionReason_(RejectionReason.MALFORMED_MESSAGE)
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._senderAgent_(whoIAm())
				.build();
	}

	private Message createRejectionTokenLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(header!=null?header.getIssuerConnector():URI.create("http://auto-generated.com"))
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("http://auto-generated.com")))
				._correlationMessage_(header!=null?header.getId():URI.create("http://auto-generated.com"))
				._rejectionReason_(RejectionReason.NOT_AUTHENTICATED)
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._senderAgent_(whoIAm())
				.build();
	}

	private Message createRejectionCommunicationLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(header!=null?header.getIssuerConnector():URI.create("http://auto-generated.com"))
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("http://auto-generated.com")))
				._correlationMessage_(header!=null?header.getId():URI.create("http://auto-generated.com"))
				._rejectionReason_(RejectionReason.NOT_FOUND)
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._senderAgent_(whoIAm())
				.build();
	}
	
	private Message createRejectionUsageControl(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(header!=null?header.getIssuerConnector():URI.create("http://auto-generated.com"))
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("http://auto-generated.com")))
				._correlationMessage_(header!=null?header.getId():URI.create("http://auto-generated.com"))
				._rejectionReason_(RejectionReason.NOT_AUTHORIZED)
				._securityToken_(dapsProvider.getDynamicAtributeToken())
				._senderAgent_(whoIAm())
				.build();
	}
}
