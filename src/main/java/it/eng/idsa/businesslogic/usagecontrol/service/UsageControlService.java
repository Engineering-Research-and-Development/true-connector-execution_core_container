package it.eng.idsa.businesslogic.usagecontrol.service;

import com.google.gson.JsonElement;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;

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
	public String createUsageControlObject(ArtifactRequestMessage artifactRequestMessage,
			ArtifactResponseMessage artifactResponseMessage, String payloadContent);

	/**
	 * Used to upload policy to Usage Control
	 * @param payloadContent
	 * @return
	 */
	public String uploadPolicy(String payloadContent);
}
