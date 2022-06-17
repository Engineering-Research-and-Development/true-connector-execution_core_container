package it.eng.idsa.businesslogic.usagecontrol.model;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsageControlObjectToEnforce {

	public UsageControlObjectToEnforce() {

	}

	@JsonProperty("payload")
	private String payload;

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public URI getTargetArtifactId() {
		return targetArtifactId;
	}

	public void setTargetArtifactId(URI targetArtifactId) {
		this.targetArtifactId = targetArtifactId;
	}

	public URI getAssignee() {
		return assignee;
	}

	public void setAssignee(URI assignee) {
		this.assignee = assignee;
	}

	public URI getAssigner() {
		return assigner;
	}

	public void setAssigner(URI assigner) {
		this.assigner = assigner;
	}

	@JsonProperty("targetArtifactId")
	private URI targetArtifactId;
	@JsonProperty("assignee")
	private URI assignee;
	@JsonProperty("assigner")
	private URI assigner;

}
