package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.NotificationService;
import com.example.adoptr_backend.service.dto.request.TokenDTO;
import com.example.adoptr_backend.service.dto.response.FirebaseTokenDTO;
import com.example.adoptr_backend.service.dto.response.NotificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@Tag(name = "Notification", description = "Notification Endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get all user notification", security = { @SecurityRequirement(name = "bearer-jwt") })
    @GetMapping("/all")
    public ResponseEntity<List<NotificationDTO>> getAllUserNotifications() {
        List<NotificationDTO> response = notificationService.getAll();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete all user notification", security = { @SecurityRequirement(name = "bearer-jwt") })
    @DeleteMapping("/all")
    public ResponseEntity<List<NotificationDTO>> deleteAllUserNotifications() {
        notificationService.deleteNotification();
        return new ResponseEntity<>(HttpStatus.OK);
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
        notificationService.sendLikeNotification(postId, "TEST");
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
