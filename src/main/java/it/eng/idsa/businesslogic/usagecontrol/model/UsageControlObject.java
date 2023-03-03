package it.eng.idsa.businesslogic.usagecontrol.model;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Schema for Usage Control Object
 * <p>
 * The IDS Usage Control Object has to be used to apply usage control
 * 
 */
public class UsageControlObject {

    /**
     * The Meta Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * 
     */
    @SerializedName("meta")
    @Expose
    private Meta meta;
    /**
     * The Payload Schema
     * <p>
     * This is the Placeholder for the Data it must be an Json Element
     * (Required)
     * 
     */
    @SerializedName("payload")
    @Expose
    private JsonElement payload;

    /**
     * No args constructor for use in serialization
     * 
     */
    public UsageControlObject() {
    }

    /**
     * Constructor
     * @param meta Meta part
     * @param payload payload part
     */
    public UsageControlObject(Meta meta, JsonElement payload) {
        super();
        this.meta = meta;
        this.payload = payload;
    }

    /**
     * The Meta Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * @return Meta part
     */
    public Meta getMeta() {
        return meta;
    }

    /**
     * The Meta Schema
     * <p>
     * An explanation about the purpose of this instance.
     * (Required)
     * @param meta Meta
     */
    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    /**
     * The Payload Schema
     * <p>
     * This is the Placeholder for the Data it must be an Json Element
     * (Required)
     * @return JsonElement representation of payload
     * 
     */
    public JsonElement getPayload() {
        return payload;
    }

    /**
     * The Payload Schema
     * <p>
     * This is the Placeholder for the Data it must be an Json Element
     * (Required)
     * @param payload JsonElement representation of payload
     * 
     */
    public void setPayload(JsonElement payload) {
        this.payload = payload;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(payload).append(meta).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UsageControlObject) == false) {
            return false;
        }
        UsageControlObject rhs = ((UsageControlObject) other);
        return new EqualsBuilder().append(payload, rhs.payload).append(meta, rhs.meta).isEquals();
    }

    /**
     * returns the payload as the specific Class if possible
     * @param <T> Typed class
     * @param clazz Clazz
     * @return Typed class
     */
    public <T> T getPayload(Class<T> clazz) {
    	final Gson gson = new Gson();
    	T payloadAsclass = gson.fromJson(gson.toJsonTree(payload), clazz);
        return payloadAsclass;
    }
}
