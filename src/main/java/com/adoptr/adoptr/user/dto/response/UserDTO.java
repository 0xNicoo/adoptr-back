package com.adoptr.adoptr.user.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String provider;
    private String providerUserId;
    private String email;
    private String name;
    private LocalDateTime createdAt;
}
