package it.eng.idsa.businesslogic.web.rest;

import io.swagger.v3.oas.annotations.Hidden;
import it.eng.idsa.businesslogic.service.impl.PasswordValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Antonio Scatoloni on 22/04/2020
 **/

@RestController
@EnableAutoConfiguration
@RequestMapping({"/notification"})
@Hidden
public class PasswordController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordValidatorService passwordValidatorService;

    @GetMapping("/password/{password}")
    @ResponseBody
    public ResponseEntity<Object> getPassword(@PathVariable String password) {
        String response;
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<String> validate = passwordValidatorService.validate(password);
        if (validate.isEmpty()) {
            status = HttpStatus.OK;
            response = passwordEncoder.encode(password);
        } else {
            response = validate.stream().map(String::valueOf).collect(Collectors.joining("\n"));
        }
        return new ResponseEntity<>(response, status);
    }

}
