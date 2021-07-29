package it.eng.idsa.businesslogic.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.businesslogic.service.SelfDescriptionService;

/**
 * @author Antonio Scatoloni on 17/07/2020
 **/

@RestController
@EnableAutoConfiguration
public class SelfDescription {
    @Autowired
    private SelfDescriptionService selfDescriptionService;


    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String selfDescriptions() {
        return selfDescriptionService.getConnectorSelfDescription();
    }
}
