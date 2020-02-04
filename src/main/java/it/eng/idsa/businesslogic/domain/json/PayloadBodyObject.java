package it.eng.idsa.businesslogic.domain.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PayloadBodyObject implements Serializable {

	private static final long serialVersionUID = -1396221101522123728L;

//	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("catalog.offers.0.resourceEndpoints.path")
	public String payload;

//	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("base64arrayType")
	public String base64arrayType;
	
//	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("base64arrayDocument")
	public String base64arrayDocument;
	
//	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("checksum")
	public String checksum;

	@Override
	public String toString() {
		return "PayloadBodyObject [payload=" + payload + ", base64arrayType=" + base64arrayType
				+ ", base64arrayDocument=" + base64arrayDocument + ", checksum=" + checksum + "]";
	}

}
