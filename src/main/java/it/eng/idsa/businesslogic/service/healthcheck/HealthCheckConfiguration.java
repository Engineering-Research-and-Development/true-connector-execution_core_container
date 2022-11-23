package it.eng.idsa.businesslogic.service.healthcheck;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("application.healthcheck")
public class HealthCheckConfiguration {

	private String dataapp;
	private String usagecontrol;
	private String daps;
	private String clearinghouse;

	private Threshold threshold;

	public static class Threshold {

		private int audit;

		public int getAudit() {
			return audit;
		}

		public void setAudit(int audit) {
			this.audit = audit;
		}
	}

	public String getDataapp() {
		return dataapp;
	}

	public void setDataapp(String dataapp) {
		this.dataapp = dataapp;
	}

	public String getUsagecontrol() {
		return usagecontrol;
	}

	public void setUsagecontrol(String usagecontrol) {
		this.usagecontrol = usagecontrol;
	}

	public String getDaps() {
		return daps;
	}

	public void setDaps(String daps) {
		this.daps = daps;
	}

	public String getClearinghouse() {
		return clearinghouse;
	}

	public void setClearinghouse(String clearinghouse) {
		this.clearinghouse = clearinghouse;
	}

	public Threshold getThreshold() {
		return threshold;
	}

	public void setThreshold(Threshold threshold) {
		this.threshold = threshold;
	}
	
}
