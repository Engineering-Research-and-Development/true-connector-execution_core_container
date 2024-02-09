package it.eng.idsa.businesslogic.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.entity.AuditLog;
import it.eng.idsa.businesslogic.repository.AuditEventRepository;

@Service
public class AuditEventService {
	@Autowired
	private AuditEventRepository auditRepository;

	public AuditLog saveAuditEvent(AuditLog auditEvent) {
		return auditRepository.save(auditEvent);
	}

	public List<AuditLog> getAllAuditEvents() {
		return auditRepository.findAll();
	}

	public List<AuditLog> getAuditEventsForDate(LocalDate date) {
		LocalDateTime startOfDay = date.atStartOfDay(); // Start of the day
		LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // End of the day

		return auditRepository.findByTimestampBetween(startOfDay, endOfDay);
	}
}
