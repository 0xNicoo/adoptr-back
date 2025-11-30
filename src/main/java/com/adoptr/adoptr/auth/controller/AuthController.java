package com.adoptr.adoptr.auth.controller;

import com.adoptr.adoptr.auth.dto.request.AuthRequestDTO;
import com.adoptr.adoptr.auth.dto.response.AuthDTO;
import com.adoptr.adoptr.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/oauth")
    public ResponseEntity<AuthDTO> authLogin(@RequestBody AuthRequestDTO request) {
        AuthDTO response = authService.authenticate(request.provider(), request.token());
        return ResponseEntity.ok(response);
    }
}
