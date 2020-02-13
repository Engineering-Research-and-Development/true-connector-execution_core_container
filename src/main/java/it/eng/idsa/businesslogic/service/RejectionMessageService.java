package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public interface RejectionMessageService {
	
	public void sendRejectionMessage(RejectionMessageType rejectionMessageType, Message message);
}
