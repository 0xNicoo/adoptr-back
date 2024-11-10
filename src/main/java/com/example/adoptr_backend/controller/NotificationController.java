package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.NotificationService;
import com.example.adoptr_backend.service.dto.request.TokenDTO;
import com.example.adoptr_backend.service.dto.response.FirebaseTokenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@Tag(name = "Notification", description = "Notification Endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Set User Firebase token", security = { @SecurityRequirement(name = "bearer-jwt") })
    @PostMapping("/user/token")
    public ResponseEntity<FirebaseTokenDTO> userToken(@Valid @RequestBody TokenDTO dto) {
        FirebaseTokenDTO response = notificationService.addUserToken(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "test", security = { @SecurityRequirement(name = "bearer-jwt") })
    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestBody Long postId) {
        notificationService.sendLikeNotification(postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
