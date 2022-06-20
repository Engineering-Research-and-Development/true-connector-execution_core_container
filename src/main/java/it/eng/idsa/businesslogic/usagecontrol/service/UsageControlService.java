package it.eng.idsa.businesslogic.usagecontrol.service;

import java.net.URI;

import com.google.gson.JsonElement;

public interface UsageControlService {

	/**
	 * Used on the Usage Control consumer side for policy enforcement
	 * @param ucObject
	 * @return
	 * @throws Exception
	 */
	public String enforceUsageControl(JsonElement ucObject) throws Exception;

	/**
	 * Used on the Usage Control provider side for creating Usage Control Object
	 * @param requestedArtifact
	 * @param payloadContent
	 * @return
	 */
	public String createUsageControlObject(URI requestedArtifact, String payloadContent);
}
