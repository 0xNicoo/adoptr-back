package com.adoptr.adoptr.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/** Sirve la carpeta de {@code LocalImageStorage}, que en S3 hace el propio bucket. */
@Configuration
@ConditionalOnProperty(name = "app.images.storage", havingValue = "local")
public class LocalImagesWebConfig implements WebMvcConfigurer {

    private final String location;

    public LocalImagesWebConfig(@Value("${app.images.local.dir}") String dir) {
        // Spring exige la barra final, y toUri solo la agrega si la carpeta ya existe.
        this.location = Path.of(dir).toAbsolutePath().normalize().toUri().toString().replaceAll("/*$", "/");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/local-images/**").addResourceLocations(location);
    }
}
