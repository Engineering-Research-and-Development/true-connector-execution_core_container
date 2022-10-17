package it.eng.idsa.businesslogic.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@RestController
@RequestMapping({ "/about" })
//exclude this endpoint from Swagger basic auth
@SecurityRequirements
public class UtilResource {
	
	@Autowired
	private BuildProperties buildProperties;
	
	@GetMapping("/version")
    @ResponseBody
    public String getVersion() {
        return buildProperties.getVersion();
    }
	

}
