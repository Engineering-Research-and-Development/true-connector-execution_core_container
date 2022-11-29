package it.eng.idsa.businesslogic.service.healthcheck;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

@Service
public class AuditLogHealthCheck {
	
	private static final Logger logger = LoggerFactory.getLogger(AuditLogHealthCheck.class);

	private HealthCheckConfiguration healthCheckConfiguration;
	
	public AuditLogHealthCheck(HealthCheckConfiguration healthCheckConfiguration) {
		super();
		this.healthCheckConfiguration = healthCheckConfiguration;
	}

	public boolean isAuditLogVolumeHealthy() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger auditLogger = context.getLogger("JSON");
		FileAppender<ILoggingEvent> auditFileAppender = (FileAppender<ILoggingEvent>) ((ch.qos.logback.classic.Logger) auditLogger).iteratorForAppenders().next();
		Path path = Paths.get(auditFileAppender.getFile());
		if(path.isAbsolute()) { 
			Path parentDir = path.getParent();
			File volume = parentDir.toFile();
			logger.info(String.format("Total space: %.2f MB", (double) volume.getTotalSpace() / 1048576));
			logger.info(String.format("Free space: %.2f MB", (double) volume.getFreeSpace() / 1048576));
			logger.info(String.format("Usable space: %.2f MB", (double) volume.getUsableSpace() / 1048576));
			int auditTreshold = healthCheckConfiguration.getThreshold().getAudit();
			if((double)(volume.getUsableSpace()) / volume.getTotalSpace() * 100 > auditTreshold) {
				logger.info("Volume trashold of '{}' not reached", auditTreshold);
				return true;
			} else {
				logger.warn("Volume trashold of '{}' reached - consider performing cleanup or backup on audit volume", auditTreshold);
				return false;
			}
		} else {
			logger.info("Path is relative");
			return true;
	    }
	}
}
