package it.eng.idsa.businesslogic.web.rest.resources;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.eng.idsa.businesslogic.entity.AuditLog;
import it.eng.idsa.businesslogic.service.AuditEventService;

@Tag(name = "Audit controller", description = "Returns audit logs.")
@RestController
@RequestMapping("/api/audit/")
public class AuditController {

	private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

	private AuditEventService auditService;

	public AuditController(AuditEventService auditService) {
		this.auditService = auditService;
	}

	@Operation(summary = "All audit logs", tags = "Audit controller - all audit logs")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "All audit logs", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class)) }) })
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<AuditLog>> getAuditLogs(
			@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		if (date != null) {
			logger.info("Fetching audit logs for date: {}", date);
			return ResponseEntity.ok(auditService.getAuditEventsForDate(date));
		} else {
			logger.info("Fetching all audit logs");
			return ResponseEntity.ok(auditService.getAllAuditEvents());
		}
	}
	
	 @ExceptionHandler(IllegalArgumentException.class)
	  public ResponseEntity handleError(HttpServletRequest req, Exception ex) {
	    logger.error("Request: " + req.getRequestURL() + " raised " + ex);

	    return ResponseEntity.badRequest().body(null);
	  }
}
