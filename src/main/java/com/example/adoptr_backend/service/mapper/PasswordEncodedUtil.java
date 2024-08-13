package com.example.adoptr_backend.service.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncodedUtil {

    private static PasswordEncoder passwordEncoder;

    public PasswordEncodedUtil(PasswordEncoder passwordEncoder) {
        PasswordEncodedUtil.passwordEncoder = passwordEncoder;
    }

    public static String encode(String password){
       return passwordEncoder.encode(password);
    }
}
