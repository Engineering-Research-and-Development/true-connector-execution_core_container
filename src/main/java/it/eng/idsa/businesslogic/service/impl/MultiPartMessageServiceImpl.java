package it.eng.idsa.businesslogic.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.MultiPartMessageService;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */


/**
 * Service Implementation for managing MultiPartMessage.
 */
@Service
@Transactional
public class MultiPartMessageServiceImpl implements MultiPartMessageService {

	@Override
	public String getHeader(String body) {
		 MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
	     return deserializedMultipartMessage.getHeaderString();
	}

	@Override
	public String getPayload(String body) {
		MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
		return deserializedMultipartMessage.getPayload();
	}

	@Override
	public Message getMessage(String body) {
		MultiPartMessage deserializedMultipartMessage = MultiPart.parseString(body);
		return deserializedMultipartMessage.getHeader();
	}

	
}
