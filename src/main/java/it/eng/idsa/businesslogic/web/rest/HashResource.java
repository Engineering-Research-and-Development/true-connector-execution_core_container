package it.eng.idsa.businesslogic.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import it.eng.idsa.businesslogic.service.HashFileService;

/**
 * @author Antonio Scatoloni on 22/04/2020
 **/

@RestController
@EnableAutoConfiguration
@RequestMapping({"/notification" })
@Hidden
public class HashResource {

    @Autowired
    private HashFileService hashService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/content/{hash}")
    @ResponseBody
    public String getPayload(@PathVariable String hash) throws Exception {
        return hashService.getContent(hash);
    }

    @GetMapping("/password/{password}")
    @ResponseBody
    public String getPassword(@PathVariable String password) throws Exception {
        return passwordEncoder.encode(password);
    }
    
}
