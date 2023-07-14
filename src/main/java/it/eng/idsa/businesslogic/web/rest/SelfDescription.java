package it.eng.idsa.businesslogic.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.BaseConnectorImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.eng.idsa.businesslogic.audit.Auditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

/**
 * @author Antonio Scatoloni on 17/07/2020
 **/


@Tag(name = "Self description controller - only valid documents", description = "Returns only offered resources that have: at least one representation with at least one artifact and at least one contract offer")
@RestController
// exclude this endpoint from Swagger basic auth
@SecurityRequirements
public class SelfDescription {
    @Autowired
    private SelfDescriptionService selfDescriptionService;

    
    @Operation(summary = "Valid self description document", tags = "Self description controller - only valid documents")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Valid self description document", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Auditable(eventType = TrueConnectorEventType.SELF_DESCRIPTION)
    @ResponseBody
    public String selfDescriptions() {
        return selfDescriptionService.getConnectorSelfDescription();
    }
}
