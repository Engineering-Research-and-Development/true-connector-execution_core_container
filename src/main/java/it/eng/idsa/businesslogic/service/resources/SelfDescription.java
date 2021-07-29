package it.eng.idsa.businesslogic.service.resources;

import de.fraunhofer.iais.eis.Connector;

public class SelfDescription {
	
	private Connector connector = null;
	
	private SelfDescription() {        
    }
    
	private static class SingletonHelper{
        private static final SelfDescription INSTANCE = new SelfDescription();
    }
    
    public static SelfDescription getInstance(){
        return SingletonHelper.INSTANCE;
    }

	public void setBaseConnector(Connector connector) {
		this.connector = connector;
	}
    
	public Connector getConnector() {
		return connector;
	}
}
