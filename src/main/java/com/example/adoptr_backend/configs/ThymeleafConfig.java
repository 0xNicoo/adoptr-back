package com.example.adoptr_backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;

@Configuration
public class ThymeleafConfig {
    @Bean
    public TemplateEngine templateEngine() {
        return new TemplateEngine();
    }
}
