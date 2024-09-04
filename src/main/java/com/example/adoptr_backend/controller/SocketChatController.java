package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.service.ChatService;
import com.example.adoptr_backend.util.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SocketChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    public SocketChatController(SimpMessagingTemplate simpMessagingTemplate,
                                ChatService chatService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat")
    public void chat(@Payload ChatMessage message){
        chatService.saveMassage(message);
        simpMessagingTemplate.convertAndSendToUser(message.getRecipientId(), "/queue", message);
    }

}
