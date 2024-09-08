package com.example.adoptr_backend.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ChatMessage {
    private String content;
    private String senderEmail;
    private String senderId;
    private String recipientEmail;
    private String recipientId;
    private String chatId;
}
