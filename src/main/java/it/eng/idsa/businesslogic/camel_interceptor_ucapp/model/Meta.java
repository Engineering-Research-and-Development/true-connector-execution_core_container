package it.eng.idsa.businesslogic.camel_interceptor_ucapp.model;

import java.net.URI;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The Meta Schema
 * <p>
 * An explanation about the purpose of this instance.
 * 
 */
public class Meta {

    /**
     * The Assigner Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * 
     */
    @SerializedName("assigner")
    @Expose
    private URI assigner;
    /**
     * The Assignee Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * 
     */
    @SerializedName("assignee")
    @Expose
    private URI assignee;
    /**
     * The Targetartifact Schema
     * <p>
     * Contains Timestamp and ArtefactId
     * (Required)
     * 
     */
    @SerializedName("targetArtifact")
    @Expose
    private TargetArtifact targetArtifact;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Meta() {
    }

    /**
     * 
     * @param assigner
     * @param assignee
     * @param targetArtifact
     */
    public Meta(URI assigner, URI assignee, TargetArtifact targetArtifact) {
        super();
        this.assigner = assigner;
        this.assignee = assignee;
        this.targetArtifact = targetArtifact;
    }

    /**
     * The Assigner Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * 
     */
    public URI getAssigner() {
        return assigner;
    }

    /**
     * The Assigner Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * 
     */
    public void setAssigner(URI assigner) {
        this.assigner = assigner;
    }

    /**
     * The Assignee Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * 
     */
    public URI getAssignee() {
        return assignee;
    }

    /**
     * The Assignee Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * 
     */
    public void setAssignee(URI assignee) {
        this.assignee = assignee;
    }

    /**
     * The Targetartifact Schema
     * <p>
     * Contains Timestamp and ArtefactId
     * (Required)
     * 
     */
    public TargetArtifact getTargetArtifact() {
        return targetArtifact;
    }

    /**
     * The Targetartifact Schema
     * <p>
     * Contains Timestamp and ArtefactId
     * (Required)
     * 
     */
    public void setTargetArtifact(TargetArtifact targetArtifact) {
        this.targetArtifact = targetArtifact;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(assigner).append(assignee).append(targetArtifact).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Meta) == false) {
            return false;
        }
        Meta rhs = ((Meta) other);
        return new EqualsBuilder().append(assigner, rhs.assigner).append(assignee, rhs.assignee).append(targetArtifact, rhs.targetArtifact).isEquals();
    }

}
