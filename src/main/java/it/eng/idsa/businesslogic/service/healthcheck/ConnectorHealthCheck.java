package it.eng.idsa.businesslogic.service.healthcheck;

public class ConnectorHealthCheck {

	private static ConnectorHealthCheck instance;
	private Boolean connectorHealth;

	private ConnectorHealthCheck() {
		// private constructor
	}

	public static ConnectorHealthCheck getInstance() {
		if (instance == null) {
			// synchronized block to remove overhead
			synchronized (ConnectorHealthCheck.class) {
				if (instance == null) {
					// if instance is null, initialize
					instance = new ConnectorHealthCheck();
					instance.setConnectorHealth(Boolean.TRUE);
				}
			}
		}
		return instance;
	}
	
	public Boolean getConnectorHealth() {
		return connectorHealth;
	}
	
	public void setConnectorHealth(boolean health) {
		this.connectorHealth = health;
	}
}
