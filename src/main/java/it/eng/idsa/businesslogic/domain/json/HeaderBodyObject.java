package it.eng.idsa.businesslogic.domain.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeaderBodyObject implements Serializable {

	private static final long serialVersionUID = 1747391698603493374L;
	
	@JsonProperty("authorizationToken")
	public AuthorizationTokenObject authorizationTokenObject;
	
	@JsonProperty("@type")
	public String type;
	
	@JsonProperty("issued")
	public String issued;
	
	@JsonProperty("issuerConnector")
	public String issuerConnector;
	
	@JsonProperty("correlationMessage")
	public String correlationMessage;
	
	@JsonProperty("transferContract")
	public String transferContract;
	
	@JsonProperty("modelVersion")
	public String modelVersion;
	
	@JsonProperty("@id")
	public String id;

	@Override
	public String toString() {
		return "HeaderBodyObject [authorizationTokenObject=" + authorizationTokenObject + ", type=" + type + ", issued="
				+ issued + ", issuerConnector=" + issuerConnector + ", correlationMessage=" + correlationMessage
				+ ", transferContract=" + transferContract + ", modelVersion=" + modelVersion + ", id=" + id + "]";
	}

}
