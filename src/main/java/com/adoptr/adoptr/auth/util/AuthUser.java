package com.adoptr.adoptr.auth.util;

public record AuthUser (
        String providerUserId,
        String email,
        String name,
        String avatar
){}