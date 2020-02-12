package it.eng.idsa.businesslogic.domain.json;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeaderBodyForOpenApiRejectMessageObject implements Serializable {

	private static final long serialVersionUID = 8054632229924726535L;
	
	@JsonProperty("@context")
	public String context;
	
	@JsonProperty("@type")
	public String type;
	
	@JsonProperty("issuerConnector")
	public String issuerConnector;
	
	@JsonProperty("issued")
	public String issued;
	
	@JsonProperty("modelVersion")
	public String modelVersion;
	
	@JsonProperty("recipientConnector")
	public ArrayList<String> recipientConnector;
	
	@JsonProperty("correlationMessage")
	public String correlationMessage;
	
	@JsonProperty("@id")
	public String id;
	
	@JsonProperty("rejectionReason")
	public RejectionReason rejectionReason;

	@Override
	public String toString() {
		return "HeaderBodyForOpenApiRejectMessageObject [context=" + context + ", type=" + type + ", issuerConnector="
				+ issuerConnector + ", issued=" + issued + ", modelVersion=" + modelVersion + ", correlationMessage="
				+ correlationMessage + ", id=" + id + ", rejectionReason=" + rejectionReason + "]";
	}
}
