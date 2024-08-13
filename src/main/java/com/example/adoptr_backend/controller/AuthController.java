package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.dto.request.UserDTOin;
import com.example.adoptr_backend.service.dto.response.TokenResponse;
import com.example.adoptr_backend.service.dto.response.UserDTO;
import com.example.adoptr_backend.service.impl.AuthService;
import com.example.adoptr_backend.service.impl.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTOin userRequestDto) {
        authService.register(userRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody UserDTOin userRequestDto) {
        UserDTO authenticatedUser = authService.authenticate(userRequestDto);
        final String accessToken = jwtService.buildToken(authenticatedUser.getEmail());
        TokenResponse tokenResponse = new TokenResponse(accessToken, authenticatedUser);
        return ResponseEntity.ok(tokenResponse);
    }

}