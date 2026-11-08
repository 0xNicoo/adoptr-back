package com.adoptr.adoptr.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Reloj único de la aplicación: las fechas de dominio se toman de acá para poder fijarlas en los tests. */
@Configuration
public class ClockConfig {

    public static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Bean
    public Clock clock() {
        return Clock.system(ZONE);
    }
}
