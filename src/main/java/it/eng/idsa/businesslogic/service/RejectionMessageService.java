package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public interface RejectionMessageService {
	
	public void sendRejectionMessage(Message requestMessage, RejectionReason rejectionReason);
}
