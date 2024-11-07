package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@Tag(name = "Email", description = "Email Endpoints")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    @Operation(summary = "Manda mail", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> report(){
        //TODO(VIKIMIRI): poner el metedo que haces vos para el mail
        //email.service ....
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
