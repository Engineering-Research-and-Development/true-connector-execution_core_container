package it.eng.idsa.businesslogic.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.eng.idsa.businesslogic.entity.AuditLog;

public interface AuditEventRepository extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findByTimestampBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
