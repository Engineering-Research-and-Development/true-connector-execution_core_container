package it.eng.idsa.businesslogic.multipart;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */


/**
 * Type of the MultipartMessage.
 */
public class MultipartMessage {
	
	private Map<String, String> httpHeaders = new HashMap<>();
	private Map<String, String> headerHeader = new HashMap<>();
	private Message headerContent = null;
	private Map<String, String> payloadHeader = new HashMap<>();
	private String payloadContent = null;
	private Map<String, String> signatureHeader= new HashMap<>();
	private String signatureContent = null;
	
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
			return new Serializer().serializePlainJson(this.headerContent);
		} catch (IOException e) {
			//TODO: throw exception
			return "";
		} 
	}

	/*
	 * Two messages are equals if the: headerContent, payloadContent and signatureContent are equals.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		
		MultipartMessage multipartMessage = (MultipartMessage) obj;
		return
			   (this.headerContent == null) ? (this.headerContent == multipartMessage.getHeaderContent()) : isHeaderContentEquqls(multipartMessage.getHeaderContent()) &&
			   (this.payloadContent == null) ? (this.payloadContent == multipartMessage.getPayloadContent()) : (this.payloadContent.equals(multipartMessage.payloadContent)) &&
			   (this.signatureContent == null) ? (this.signatureContent == multipartMessage.getSignatureContent()) : (this.getSignatureContent().equals(multipartMessage.getSignatureContent()));
	}
	
	@Override
	public final int hashCode() {
		
		int result = 17;
		if(this.headerContent != null) {
			result = 31 * result + this.headerContent.hashCode();
		}
		if(this.payloadContent != null) {
			result = 31 * result + this.payloadContent.hashCode();
		}
		if(this.signatureContent != null) {
			result = 31 * result + this.signatureContent.hashCode();
		}
		
		return result;
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
	
	// TODO: check this in the documentation: This should be adapted to the every new version of the: de.fraunhofer.iais.eis.Message
	// Problem is on the Fraunhofer: In the class is not implemented method equals for the de.fraunhofer.iais.eis.Message
	private boolean isHeaderContentEquqls(Message headerContent) {
		return (this.headerContent.getContentVersion() == null) ? (this.headerContent.getContentVersion() == headerContent.getContentVersion()) : (this.headerContent.getContentVersion().equals(headerContent.getContentVersion())) &&
			   (this.headerContent.getCorrelationMessage() == null) ? (this.headerContent.getCorrelationMessage() == headerContent.getCorrelationMessage()) : (this.headerContent.getCorrelationMessage().equals(headerContent.getCorrelationMessage())) &&
			   (this.headerContent.getIssued() == null) ? (this.headerContent.getIssued() == headerContent.getIssued()) : (this.headerContent.getIssued().equals(headerContent.getIssued())) &&
			   (this.headerContent.getIssuerConnector() == null) ? (this.headerContent.getIssuerConnector() == headerContent.getIssuerConnector()) : (this.headerContent.getIssuerConnector().equals(headerContent.getIssuerConnector())) &&
			   (this.headerContent.getModelVersion() == null) ? (this.headerContent.getModelVersion() == headerContent.getModelVersion()) : (this.headerContent.getModelVersion().equals(headerContent.getModelVersion())) &&
			   (this.headerContent.getRecipientAgent() == null) ? (this.headerContent.getRecipientAgent() == headerContent.getRecipientAgent()) : (this.headerContent.getRecipientAgent().equals(headerContent.getRecipientAgent())) &&
			   (this.headerContent.getRecipientConnector() == null) ? (this.headerContent.getRecipientConnector() == headerContent.getRecipientConnector()) : (this.headerContent.getRecipientConnector().equals(headerContent.getRecipientConnector())) &&
			   (this.headerContent.getSenderAgent() == null) ? (this.headerContent.getSenderAgent() == headerContent.getSenderAgent()) : (this.headerContent.getSenderAgent().equals(headerContent.getSenderAgent())) &&		   
			   (this.headerContent.getTransferContract() == null) ? (this.headerContent.getTransferContract() == headerContent.getTransferContract()) : (this.headerContent.getTransferContract().equals(headerContent.getTransferContract())) &&
			   (this.headerContent.getId() == null) ? (this.headerContent.getId() == headerContent.getId()) : (this.headerContent.getId().equals(headerContent.getId()));	   
	}
	
}
