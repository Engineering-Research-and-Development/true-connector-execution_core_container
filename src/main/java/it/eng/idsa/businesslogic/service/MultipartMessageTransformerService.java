package it.eng.idsa.businesslogic.service;

import it.eng.idsa.businesslogic.multipart.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public interface MultipartMessageTransformerService {

	public MultipartMessage parseMultipartMessage(String message);
	public MultipartMessage parseMultipartMessage(String message, String contentType);
	public String multipartMessagetoString(MultipartMessage message);
	public String multipartMessagetoString(MultipartMessage message, boolean includeHttpHeaders);
	
}
