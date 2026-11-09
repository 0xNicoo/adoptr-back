package com.adoptr.adoptr.shared.storage;

import com.adoptr.adoptr.shared.exception.custom.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Guarda las imágenes en una carpeta, sin AWS. Solo para desarrollo: se activa con {@code IMAGE_STORAGE=local}. */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.images.storage", havingValue = "local")
public class LocalImageStorage implements ImageStorage {

    private final Path root;
    private final String publicBaseUrl;

    public LocalImageStorage(@Value("${app.images.local.dir}") String dir,
                             @Value("${app.images.local.base-url}") String publicBaseUrl) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
        log.warn("Imágenes en disco local ({}): usar solo en desarrollo", root);
    }

    @Override
    public void upload(String key, ProcessedImage image) {
        Path target = root.resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, image.bytes());
        } catch (IOException e) {
            log.error("No se pudo guardar {} en {}", key, root, e);
            throw new ServiceUnavailableException(ImageError.STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(root.resolve(key));
        } catch (IOException e) {
            log.error("No se pudo borrar {} de {}: queda huérfano", key, root, e);
        }
    }

    @Override
    public String publicUrl(String key) {
        return publicBaseUrl + "/" + key;
    }
}
