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
public class RejectionReason implements Serializable {

	private static final long serialVersionUID = 7756975655696242181L;

	@JsonProperty("@id")
	public String id;

	@Override
	public String toString() {
		return "RejectionReason [id=" + id + "]";
	}
	
}
