package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Message;


/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing Clearing House.
 */
public interface ClearingHouseService {
	public boolean registerTransaction(Message message);

}
