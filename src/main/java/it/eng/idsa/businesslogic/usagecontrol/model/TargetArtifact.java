package it.eng.idsa.businesslogic.usagecontrol.model;

import java.net.URI;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
* The Targetartifact Schema
* <p>
* Contains Timestamp and ArtefactId
* 
*/
public class TargetArtifact {

   /**
    * The Creationdate Schema
    * <p>
    * An explanation about the purpose of this instance.
    * (Required)
    * 
    */
   @SerializedName("creationDate")
   @Expose
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
   private ZonedDateTime creationDate;
   /**
    * The @id Schema
    * <p>
    * An explanation about the purpose of this instance.
    * (Required)
    * 
    */
   @SerializedName("@id")
   @Expose
   private URI id;

   /**
    * No args constructor for use in serialization
    * 
    */
   public TargetArtifact() {
   }

   /**
    * Constructor
    * @param creationDate creation date
    * @param id Id as URI
    */
   public TargetArtifact(ZonedDateTime creationDate, URI id) {
       super();
       this.creationDate = creationDate;
       this.id = id;
   }

   /**
    * The Creationdate Schema
    * <p>
    * An explanation about the purpose of this instance.
    * (Required)
    * @return ZonedDateTime
    */
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
   public ZonedDateTime getCreationDate() {
       return creationDate;
   }

   /**
    * The Creationdate Schema
    * <p>
    * An explanation about the purpose of this instance.
    * (Required)
    * @param creationDate Ceation Date to set
    */
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
   public void setCreationDate(ZonedDateTime creationDate) {
       this.creationDate = creationDate;
   }

   /**
    * The @id Schema
    * <p>
    * An explanation about the purpose of this instance.
    * (Required)
    * @return URI returns Id
    */
   @JsonProperty("@id")
   public URI getId() {
       return id;
   }

   /**
    * The @id Schema
    * <p>
    * An explanation about the purpose of this instance.
    * (Required)
    * @param id URI
    */
   @JsonProperty("@id")
   public void setId(URI id) {
       this.id = id;
   }

   @Override
   public int hashCode() {
       return new HashCodeBuilder().append(creationDate).append(id).toHashCode();
   }

   @Override
   public boolean equals(Object other) {
       if (other == this) {
           return true;
       }
       if ((other instanceof TargetArtifact) == false) {
           return false;
       }
       TargetArtifact rhs = ((TargetArtifact) other);
       return new EqualsBuilder().append(creationDate, rhs.creationDate).append(id, rhs.id).isEquals();
   }

}
