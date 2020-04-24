package it.eng.idsa.businesslogic.web.rest;

import it.eng.idsa.businesslogic.service.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

/**
 * @author Antonio Scatoloni on 22/04/2020
 **/

@RestController
@EnableAutoConfiguration
@RequestMapping({"/notification" })
public class HashResource {

    @Autowired
    private HashService hashService;

    @GetMapping("/content/{hash}")
    @ResponseBody
    public String getPayload(@PathVariable String hash) throws Exception {
        return hashService.getContent(hash);
    }


}
