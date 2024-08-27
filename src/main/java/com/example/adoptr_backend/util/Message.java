package com.example.adoptr_backend.util;

import lombok.Builder;

@Builder
public record Message(String username, String message) { }