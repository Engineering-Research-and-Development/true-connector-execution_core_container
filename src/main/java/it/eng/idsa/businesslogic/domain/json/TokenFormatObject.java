package it.eng.idsa.businesslogic.domain.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenFormatObject {

	@JsonProperty("@id")
	public String id;

	@Override
	public String toString() {
		return "TokenFormat [id=" + id + "]";
	}
	
}
