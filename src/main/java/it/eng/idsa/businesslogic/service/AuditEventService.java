package it.eng.idsa.businesslogic.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.entity.AuditLog;
import it.eng.idsa.businesslogic.repository.AuditEventRepository;
import it.eng.idsa.businesslogic.util.AES256;

@Service
public class AuditEventService {
	@Autowired
	private AuditEventRepository auditRepository;

	public AuditLog saveAuditEvent(AuditLog auditEvent) {
		return auditRepository.save(auditEvent);
	}

	public List<AuditLog> getAllAuditEvents() {
		return auditRepository.findAll()
				.parallelStream()
				.map(this::decryptAuditLog)
				.collect(Collectors.toList());
	}

	public List<AuditLog> getAuditEventsForDate(LocalDate date) {
		LocalDateTime startOfDay = date.atStartOfDay(); // Start of the day
		LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // End of the day

		return auditRepository.findByTimestampBetween(startOfDay, endOfDay)
				.parallelStream()
				.map(this::decryptAuditLog)
				.collect(Collectors.toList());
	}
	
	private AuditLog decryptAuditLog(AuditLog auditLog) {
		AuditLog a = new AuditLog();
		a.setId(auditLog.getId());
		a.setEvent(AES256.decrypt(auditLog.getEvent()));
		a.setTimestamp(auditLog.getTimestamp());
		return a;
	}
}
