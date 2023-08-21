package it.eng.idsa.businesslogic.usagecontrol.service;

import java.net.URI;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;

public interface UsageControlService {

	/**
	 * Used on the Usage Control consumer side for policy enforcement
	 * @param contractAgreementUri contract agreement uri
	 * @param requestedArtifact requested artifact uri
	 * @param payload Payload for enforcing
	 * @return result response from REST call
	 * @throws Exception exception in case of error
	 */
	String enforceUsageControl(URI contractAgreementUri, URI requestedArtifact, String payload) throws Exception;

	/**
	 * Used on the Usage Control provider side for creating Usage Control Object
	 * @param artifactRequestMessage IDS ArtifactRequestMessage
	 * @param artifactResponseMessage IDS ArtifactResponseMessage
	 * @param payloadContent Payload
	 * @return result response from REST call
	 */
	String createUsageControlObject(ArtifactRequestMessage artifactRequestMessage,
			ArtifactResponseMessage artifactResponseMessage, String payloadContent);

	/**
	 * Used to upload policy to Usage Control
	 * @param payloadContent Policy for uploading
	 * @return result response from REST call
	 */
	String uploadPolicy(String payloadContent);

	/**
	 * Used to rollback policy upload if clearing house logging fails
	 * @param contractAgreementUUID ID of the contract agreement to rollback
	 */
	void rollbackPolicyUpload(String contractAgreementUUID);
	
	/**
	 * Check the availability of the usage control
	 * @param usageContolHealthEndpoint endpoint where UC app exposes health endpoint
	 * @return usage control available or not
	 */
	boolean isUsageControlAvailable(String usageContolHealthEndpoint);
}
