package it.eng.idsa.businesslogic.usagecontrol.model;

public class IdsUseObject {
	
    public String targetDataUri;
    public IdsMsgTarget msgTarget;
    public Object dataObject;
    
	public String getTargetDataUri() {
		return targetDataUri;
	}
	public void setTargetDataUri(String targetDataUri) {
		this.targetDataUri = targetDataUri;
	}
	public IdsMsgTarget getMsgTarget() {
		return msgTarget;
	}
	public void setMsgTarget(IdsMsgTarget msgTarget) {
		this.msgTarget = msgTarget;
	}
	public Object getDataObject() {
		return dataObject;
	}
	public void setDataObject(Object dataObject) {
		this.dataObject = dataObject;
	}
    
    

}
