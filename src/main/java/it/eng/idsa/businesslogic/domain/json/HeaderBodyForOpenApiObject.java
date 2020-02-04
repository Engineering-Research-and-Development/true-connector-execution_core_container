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
public class HeaderBodyForOpenApiObject implements Serializable{
	
	private static final long serialVersionUID = 6874679256752471560L;

	@JsonProperty("@type")
	public String type;
	
	@JsonProperty("issuerConnector")
	public String issuerConnector;
	
	@JsonProperty("modelVersion")
	public String modelVersion;
	
	@JsonProperty("issued")
	public String issued;
	
	@JsonProperty("@id")
	public String id;

	@Override
	public String toString() {
		return "HeaderBodyJson [type=" + type + ", issuerConnector=" + issuerConnector + ", modelVersion="
				+ modelVersion + ", issued=" + issued + ", id=" + id + "]";
	}
	
}
