package it.eng.idsa.businesslogic.domain.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class MultipartMessageObject implements Serializable {

	private HeaderBodyObject header;
	private PayloadBodyObject payload;
	
	public HeaderBodyObject getHeader() {
		return header;
	}
	public void setHeader(HeaderBodyObject header) {
		this.header = header;
	}
	public PayloadBodyObject getPayload() {
		return payload;
	}
	public void setPayload(PayloadBodyObject payload) {
		this.payload = payload;
	}
	@Override
	public String toString() {
		return "MultipartMessageJson [header=" + header + ", payload=" + payload + "]";
	}
	
}
