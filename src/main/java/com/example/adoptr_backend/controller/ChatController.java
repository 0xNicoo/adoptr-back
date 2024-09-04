package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ChatService;
import com.example.adoptr_backend.service.dto.response.ChatDTO;
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
    @Operation(summary = "Obtiene un chat por publicationId", security = { @SecurityRequirement(name = "bearer-jwt") })
    public ResponseEntity<ChatDTO> getByPublication(@PathVariable Long publicationId){
        ChatDTO response = chatService.getByPublication(publicationId);
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

}
