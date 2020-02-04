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
public class AuthorizationTokenObject implements Serializable{

	private static final long serialVersionUID = 2342642791112360993L;
	
	@JsonProperty("@type")
	public String type;
	
	@JsonProperty("@id")
	public String id;
	
	@JsonProperty("tokenFormat")
	public TokenFormatObject tokenFormat;
	
	@JsonProperty("tokenValue")
	public String tokenValue;

	@Override
	public String toString() {
		return "AuthorizationToken [type=" + type + ", id=" + id + ", tokenFormat=" + tokenFormat + ", tokenValue="
				+ tokenValue + "]";
	}
	
}
