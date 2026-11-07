package com.adoptr.adoptr.location.loader;

import com.adoptr.adoptr.location.repository.ProvinceRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Carga provincias y localidades de la copia de georef al arrancar, solo si la
 * tabla de provincias está vacía
 */
@Slf4j
@Component
public class LocationDataLoader implements ApplicationRunner {

    static final String RESOURCE = "data/georef/locations.json";

    private final ProvinceRepository provinceRepository;
    private final EntityManager entityManager;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public LocationDataLoader(ProvinceRepository provinceRepository, EntityManager entityManager) {
        this.provinceRepository = provinceRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        load();
    }

    /** @return cantidad de localidades cargadas  */
    int load() {
        if (provinceRepository.count() > 0) {
            log.info("Catálogo de ubicaciones ya cargado; no se importa {}", RESOURCE);
            return 0;
        }
        GeorefCatalog catalog = GeorefCatalog.from(readSnapshot());
        catalog.provinces().forEach(entityManager::persist);
        catalog.localities().forEach(entityManager::persist);
        log.info("Catálogo de ubicaciones importado: {} provincias, {} localidades",
                catalog.provinces().size(), catalog.localities().size());
        return catalog.localities().size();
    }

    GeorefSnapshot readSnapshot() {
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            return jsonMapper.readValue(input, GeorefSnapshot.class);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + RESOURCE, e);
        }
    }
}
