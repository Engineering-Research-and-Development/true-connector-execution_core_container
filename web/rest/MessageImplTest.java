package it.eng.idsa.businesslogic.web.rest;

import java.net.URI;
import java.util.ArrayList;

import javax.validation.constraints.NotNull;
import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Token;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("ids:Message")
public class MessageImplTest implements Message{

	@Override
	public @NotNull URI getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toRdf() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModelVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XMLGregorianCalendar getIssued() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getIssuerConnector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<? extends URI> getRecipientConnectors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<? extends URI> getRecipientAgents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getCorrelationMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getSenderAgent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Token getSecurityToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Token getAuthorizationToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getTransferContract() {
		// TODO Auto-generated method stub
		return null;
	}





}
