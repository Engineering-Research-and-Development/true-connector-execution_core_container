package it.eng.idsa.businesslogic.web.rest;

import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Antonio Scatoloni on 17/07/2020
 **/

@RestController
@EnableAutoConfiguration
public class SelfDescription {
    @Autowired
    private SelfDescriptionService selfDescriptionService;


    @GetMapping("/")
    @ResponseBody
    public String selfDescriptions() {
        return selfDescriptionService.getConnectorAsString();
    }


}
