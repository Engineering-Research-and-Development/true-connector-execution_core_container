package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Message;

/**
 * @author Antonio Scatoloni on 17/07/2020
 **/

public interface SelfDescriptionService {
	
	Connector getConnector();
	
	/**
	 * Returns String representation of BaseConnector class instance
	 * @return
	 */
    String getConnectorSelfDescription();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorAvailableMessage}
     * @return
     */
    Message getConnectorAvailbilityMessage();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage}
     * @return
     */
    Message getConnectorUpdateMessage();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorUnavailableMessage}
     * @return
     */
    Message getConnectorUnavailableMessage();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorInactiveMessage}
     * @return
     */
    Message getConnectorInactiveMessage();
    
    /**
     * 
     * @return
     */
    Message getConnectorQueryMessage();
}
