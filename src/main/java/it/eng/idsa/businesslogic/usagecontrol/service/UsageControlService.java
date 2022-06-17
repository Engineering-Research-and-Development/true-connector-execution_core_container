package it.eng.idsa.businesslogic.usagecontrol.service;

import com.google.gson.JsonElement;

import de.fraunhofer.iais.eis.Message;

public interface UsageControlService {

	public String enforceUsageControl(Message message, JsonElement ucObject) throws Exception;
}
