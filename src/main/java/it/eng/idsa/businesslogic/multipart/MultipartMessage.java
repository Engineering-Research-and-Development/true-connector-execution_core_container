package it.eng.idsa.businesslogic.multipart;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.multipart.service.MultipartMessageService;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */


/**
 * Type of the MultipartMessage.
 */
public class MultipartMessage {
	
	@Autowired
	MultipartMessageService multipartMessageService;
	
	private Map<String, String> httpHeaders;
	private Map<String, String> headerHeader;
	private Message headerContent;
	private Map<String, String> payloadHeader;
	private String payloadContent;
	private Map<String, String> signatureHeader;
	private String signatureContent;
	
	public MultipartMessage() {
		super();
	}
	
	public MultipartMessage(Map<String, String> httpHeaders, Map<String, String> headerHeader, Message headerContent,
			Map<String, String> payloadHeader, String payloadContent, Map<String, String> signatureHeader,
			String signatureContent) {
		super();
		this.httpHeaders = httpHeaders;
		this.headerHeader = headerHeader;
		this.headerContent = headerContent;
		this.payloadHeader = payloadHeader;
		this.payloadContent = payloadContent;
		this.signatureHeader = signatureHeader;
		this.signatureContent = signatureContent;
	}

	public Map<String, String> getHttpHeaders() {
		return httpHeaders;
	}
	
	public String getHeaderContentString() {
		try {
			return SerializationHelper.getInstance().toJsonLD(this.headerContent);
		} catch (IOException e) {
			return "";
		} 
	}

	public Map<String, String> getHeaderHeader() {
		return headerHeader;
	}

	public Message getHeaderContent() {
		return headerContent;
	}

	public Map<String, String> getPayloadHeader() {
		return payloadHeader;
	}

	public String getPayloadContent() {
		return payloadContent;
	}

	public Map<String, String> getSignatureHeader() {
		return signatureHeader;
	}

	public String getSignatureContent() {
		return signatureContent;
	}
	
	public String toString() {
		return multipartMessageService.toString(this);
	}
	
}
