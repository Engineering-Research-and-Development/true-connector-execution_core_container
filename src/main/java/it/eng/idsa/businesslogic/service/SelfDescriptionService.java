package it.eng.idsa.businesslogic.service;

import java.net.URISyntaxException;

import javax.xml.datatype.DatatypeConfigurationException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;

/**
 * @author Antonio Scatoloni on 17/07/2020
 **/

public interface SelfDescriptionService {
	/**
	 * Returns String representation of BaseConnector class instance
	 * @return
	 */
    String getConnectorAsString();
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorAvailableMessage}
     * @return
     * @throws ConstraintViolationException
     * @throws URISyntaxException
     * @throws DatatypeConfigurationException
     */
    Message getConnectorAvailbilityMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException;
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage}
     * @return
     * @throws ConstraintViolationException
     * @throws URISyntaxException
     * @throws DatatypeConfigurationException
     */
    Message getConnectorUpdateMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException;
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorUnavailableMessage}
     * @return
     * @throws ConstraintViolationException
     * @throws URISyntaxException
     * @throws DatatypeConfigurationException
     */
    Message getConnectorUnavailableMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException;
    
    /**
     * Create String instance of {@link de.fraunhofer.iais.eis.ConnectorInactiveMessage}
     * @return
     * @throws ConstraintViolationException
     * @throws URISyntaxException
     * @throws DatatypeConfigurationException
     */
    Message getConnectorInactiveMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException;
    
    /**
     * 
     * @return
     * @throws ConstraintViolationException
     * @throws URISyntaxException
     * @throws DatatypeConfigurationException
     */
    Message getConnectorQueryMessage() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException;
}
