package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Message;


/**
 * @author Milan Karajovic and Gabriele De Luca
 */

/**
 * Service Interface for managing Clearing House.
 */
public interface ClearingHouseService {
	String createProcessIdAtClearingHouse(Message contractAgreement, String agreement);

	//ORBITER IMPLEMENTATION - DEPRECATED
	//public boolean registerTransaction(Message message);
	public boolean registerTransaction(Message message, String payload);

}
