package com.adoptr.adoptr.location.loader;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.ProvinceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LocationDataLoaderTest {

    private final ProvinceRepository provinceRepository = mock(ProvinceRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final LocationDataLoader loader = new LocationDataLoader(provinceRepository, entityManager);

    @Test
    void conCatalogoYaCargadoNoImportaNada() {
        when(provinceRepository.count()).thenReturn(24L);

        assertThat(loader.load()).isZero();
        verifyNoInteractions(entityManager);
    }

    @Test
    void conTablaVaciaImportaProvinciasYLocalidades() {
        when(provinceRepository.count()).thenReturn(0L);

        int loaded = loader.load();

        assertThat(loaded).isGreaterThan(3000);
        verify(entityManager, times(24)).persist(any(Province.class));
        verify(entityManager, times(loaded)).persist(any(Locality.class));
    }

    @Test
    void laCopiaDeGeorefEsConsistente() {
        GeorefCatalog catalog = GeorefCatalog.from(loader.readSnapshot());

        assertThat(catalog.provinces()).hasSize(24);
        Set<Long> ids = new HashSet<>();
        catalog.localities().forEach(l -> assertThat(ids.add(l.getId())).as("id repetido %s", l.getId()).isTrue());

        // Dentro de cada provincia ningún nombre para mostrar se repite: siempre se pueden distinguir.
        Map<Long, List<String>> displayNamesByProvince = catalog.localities().stream()
                .collect(Collectors.groupingBy(l -> l.getProvince().getId(),
                        Collectors.mapping(Locality::getDisplayName, Collectors.toList())));
        displayNamesByProvince.forEach((provinceId, names) ->
                assertThat(names).as("provincia %s", provinceId).doesNotHaveDuplicates());

        // CABA se ofrece por barrios.
        assertThat(catalog.localities())
                .filteredOn(l -> l.getProvince().getId() == 2L)
                .extracting(Locality::getName)
                .contains("Palermo", "Flores", "Ciudad de Buenos Aires");
    }
}
