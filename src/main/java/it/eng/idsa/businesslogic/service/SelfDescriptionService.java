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
	 * @return String representation
	 */
    String getConnectorSelfDescription();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage}
     * @return IDS ConnectorUpdateMessage
     */
    Message getConnectorAvailbilityMessage();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage}
     * @return ConnectorUpdateMessage
     */
    Message getConnectorUpdateMessage();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorUnavailableMessage}
     * @return ConnectorUnavailableMessage
     */
    Message getConnectorUnavailableMessage();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorUnavailableMessage}
     * @return IDS ConnectorUnavailableMessage
     */
    Message getConnectorInactiveMessage();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.QueryMessage}
     * @return IDS QueryMessage
     */
    Message getConnectorQueryMessage();
}
