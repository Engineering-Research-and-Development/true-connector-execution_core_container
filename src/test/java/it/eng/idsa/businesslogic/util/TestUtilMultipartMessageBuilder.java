package it.eng.idsa.businesslogic.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class TestUtilMultipartMessageBuilder {
	
	private Map<String, String> httpHeaders = new HashMap<>();
	private Map<String, String> headerHeader = new HashMap<>();
	private Message headerContent = null;
	private Map<String, String> payloadHeader = new HashMap<>();
	private String payloadContent = null;
	private Map<String, String> signatureHeader = new HashMap<>();
	private String signatureContent = null;
	private String token = null;
	
	
	
	public TestUtilMultipartMessageBuilder withArtifactRequestMessage() {
		this.headerContent = TestUtilMessageService.getArtifactRequestMessage();
		return this;
	}
	
	public TestUtilMultipartMessageBuilder withArtifactResponseMessage() {
		this.headerContent = TestUtilMessageService.getArtifactResponseMessage();
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
				                    signatureContent,
				                    token
				                    );
	}

}
