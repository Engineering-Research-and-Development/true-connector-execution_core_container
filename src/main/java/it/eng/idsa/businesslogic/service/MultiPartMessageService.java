package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Message;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing MultiPartMessage.
 */
public interface MultiPartMessageService {
	public String getHeader(String body);
	public String getPayload(String body);

	public Message getMessage(String body);
	public Message getMessage(Object header);
	public String addToken(Message message, String token);
}
