package it.eng.idsa.businesslogic.multipart;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.NotificationMessage;
import it.eng.idsa.businesslogic.multipart.service.MultipartMessageService;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */


/**
 * Builder for the MultipartMessage.
 */

public class MultipartMessageBuilder {
	
	private static final Logger logger = LogManager.getLogger(MultipartMessageBuilder.class);
		
	private Map<String, String> httpHeaders;
	private Map<String, String> headerHeader;
	private Message headerContent;
	private Map<String, String> payloadHeader;
	private String payloadContent;
	private Map<String, String> signatureHeader;
	private String signatureContent;
	
	public MultipartMessageBuilder withHttpHeader(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
		return this;
	}
	
	public MultipartMessageBuilder withHeaderHeader(Map<String, String> headerHeader) {
		this.headerHeader = headerHeader;
		return this;
	}
	
	public MultipartMessageBuilder withHeaderContent(Message headerContent) {
		this.headerContent = headerContent;
		return this;
	}
	
	public MultipartMessageBuilder withHeaderContent(String headerContent) {
		try {
			this.headerContent = (Message) SerializationHelper.getInstance().fromJsonLD(headerContent,
					Message.class);
		} catch (IOException e) {
			logger.error("Could not deserialize header");
		}

		if (this.headerContent == null) {
			try {
				this.headerContent = (Message) SerializationHelper.getInstance().fromJsonLD(headerContent,
						NotificationMessage.class);
			} catch (IOException e) {
				logger.error("Could not deserialize header");
			}
		}
		return this;
	}
	
	public MultipartMessageBuilder withPayloadHeader(Map<String, String> payloadHeader) {
		this.payloadHeader = payloadHeader;
		return this;
	}
	
	public MultipartMessageBuilder withPayloadContent(String payloadContent) {
		this.payloadContent = payloadContent;
		return this;
	}
	
	public MultipartMessageBuilder withSignatureHeader(Map<String, String> signatureHeader) {
		this.signatureHeader = signatureHeader;
		return this;
	}
	
	public MultipartMessageBuilder withSignatureContent(String signatureContent) {
		this.signatureContent = signatureContent;
		return this;
	}
	
	public MultipartMessage build() {
		return new MultipartMessage(
									httpHeaders, 
				                    headerHeader,
				                    headerContent,
				                    payloadHeader, 
				                    payloadContent,
				                    signatureHeader,
				                    signatureContent
				                    );
	}

}
