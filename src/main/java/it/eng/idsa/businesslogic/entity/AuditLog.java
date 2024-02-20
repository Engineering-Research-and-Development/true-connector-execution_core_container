package it.eng.idsa.businesslogic.entity;

import java.time.LocalDateTime;
import it.eng.idsa.businesslogic.util.AES256;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "AuditLogs")
public class AuditLog {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonProperty("id")
	private Long id;
	@JsonProperty("timestamp")
	private LocalDateTime timestamp;
	@JsonProperty("event")
	@Column(columnDefinition = "TEXT")
	private String event;

	public AuditLog() {
	}

	public AuditLog(String event) {
		this.event = AES256.encrypt(event);
		this.timestamp = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

}
