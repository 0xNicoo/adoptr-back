package com.example.adoptr_backend.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserChatDTO {
    private ProfileDTO profile;
    private Long chatId;
}
