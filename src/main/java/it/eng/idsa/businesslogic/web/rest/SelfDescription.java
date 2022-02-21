package it.eng.idsa.businesslogic.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

/**
 * @author Antonio Scatoloni on 17/07/2020
 **/


@Tag(name = "Self description controller", description = "Returns document as is, regardless if it's valid or not")
@RestController
@EnableAutoConfiguration
public class SelfDescription {
    @Autowired
    private SelfDescriptionService selfDescriptionService;

    
    @Operation(summary = "Self description document", tags = "Self description controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Self description document without validation", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String selfDescriptions() {
        return selfDescriptionService.getConnectorSelfDescription();
    }
}
