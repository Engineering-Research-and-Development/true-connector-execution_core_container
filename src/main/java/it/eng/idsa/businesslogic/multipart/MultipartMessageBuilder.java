package it.eng.idsa.businesslogic.multipart;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.NotificationMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

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
		
	private Map<String, String> httpHeaders = new HashMap<>();
	private Map<String, String> headerHeader = new HashMap<>();
	private Message headerContent = null;
	private Map<String, String> payloadHeader = new HashMap<>();
	private String payloadContent = null;
	private Map<String, String> signatureHeader = new HashMap<>();
	private String signatureContent = null;
	
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
			this.headerContent = new Serializer().deserialize(headerContent, Message.class);
		} catch (IOException e) {
			logger.error("Could not deserialize header");
			e.printStackTrace();
		}

		// TODO: Check is this if necessary
		if (this.headerContent == null) {
			try {
				this.headerContent = new Serializer().deserialize(headerContent, NotificationMessage.class);
			} catch (IOException e) {
				logger.error("Could not deserialize header");
				e.printStackTrace();
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
