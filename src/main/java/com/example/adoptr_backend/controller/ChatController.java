package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ChatService;
import com.example.adoptr_backend.service.dto.response.ChatDTO;
import com.example.adoptr_backend.service.dto.response.PublicationChatDTO;
import com.example.adoptr_backend.service.dto.response.UserChatDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
@Tag(name = "Chat", description = "Chat Endpoints")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/publication/{publicationId}")
    @Operation(summary = "Obtiene los chat de una publicacion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ChatDTO>> getByPublication(@PathVariable Long publicationId){
        List<ChatDTO> response = chatService.getPublicationChats(publicationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtiene todos los chats de un usuario", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<ChatDTO>> getChats(){
        List<ChatDTO> response = chatService.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un chat", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ChatDTO> get(@PathVariable Long id){
        ChatDTO response = chatService.get(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/publication/all")
    @Operation(summary = "Obtiene los chat agrupados por publicacion", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<PublicationChatDTO>> getAllGroupByPublication(){
        List<PublicationChatDTO> response = chatService.getAllGroupByPublication();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/all")
    @Operation(summary = "Obtiene los chat agrupados por usuario", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<List<UserChatDTO>> getAllGroupByUser(){
        List<UserChatDTO> response = chatService.getAllGroupByUsers();
        return ResponseEntity.ok(response);
    }

}
