package com.adoptr.adoptr.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTOin {
    private String provider;
    String providerUserId;
    String email;
    String name;
}
