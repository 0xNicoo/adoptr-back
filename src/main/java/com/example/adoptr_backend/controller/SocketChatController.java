package com.example.adoptr_backend.controller;

import com.example.adoptr_backend.util.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SocketChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public SocketChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chat")
    public void chat(@Payload ChatMessage message){
        System.out.println(message.getContent());
        simpMessagingTemplate.convertAndSendToUser(message.getRecipientId(), "/queue", message);
    }

}
