package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.EmailService;
import com.example.adoptr_backend.service.dto.request.EmailDTOin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/email")
@AllArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "Send an email", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<String> sendEmail(@RequestBody EmailDTOin dto){
        emailService.sendEmail(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
