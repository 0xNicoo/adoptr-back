package com.example.adoptr_backend.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ChatMessage {
    private String content;
    private String userSenderEmail;
    private String userSenderId;
    private String userReceiverEmail;
    private String userReceiverId;
    private String chatId;
}
