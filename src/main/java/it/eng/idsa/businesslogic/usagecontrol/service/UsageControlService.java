package it.eng.idsa.businesslogic.usagecontrol.service;

import java.net.URI;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;

public interface UsageControlService {

	/**
	 * Used on the Usage Control consumer side for policy enforcement
	 * @param contractAgreementUri
	 * @param payload
	 * @return
	 * @throws Exception
	 */
	String enforceUsageControl(URI contractAgreementUri, String payload) throws Exception;

	/**
	 * Used on the Usage Control provider side for creating Usage Control Object
	 * @param requestedArtifact
	 * @param payloadContent
	 * @return
	 */
	String createUsageControlObject(ArtifactRequestMessage artifactRequestMessage,
			ArtifactResponseMessage artifactResponseMessage, String payloadContent);

	/**
	 * Used to upload policy to Usage Control
	 * @param payloadContent
	 * @return
	 */
	String uploadPolicy(String payloadContent);
	
	/**
	 * Check the availability of the usage control
	 * @return
	 */
	boolean isUsageControlAvailable();
}
