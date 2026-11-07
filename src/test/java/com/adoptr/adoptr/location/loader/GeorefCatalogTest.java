package com.adoptr.adoptr.location.loader;

import com.adoptr.adoptr.location.loader.GeorefSnapshot.LocalityRow;
import com.adoptr.adoptr.location.loader.GeorefSnapshot.ProvinceRow;
import com.adoptr.adoptr.location.model.Locality;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeorefCatalogTest {

    private static GeorefSnapshot snapshot(List<LocalityRow> localities) {
        return new GeorefSnapshot("Servicio Georef", "CC BY 4.0", "2026-11-05",
                List.of(new ProvinceRow("06", "Buenos Aires"), new ProvinceRow("86", "Santiago del Estero")),
                localities);
    }

    private static Map<Long, Locality> byId(GeorefCatalog catalog) {
        return catalog.localities().stream().collect(Collectors.toMap(Locality::getId, Function.identity()));
    }

    @Test
    void usaElIdOficialComoNumero() {
        GeorefCatalog catalog = GeorefCatalog.from(snapshot(List.of(
                new LocalityRow("06441030", "La Plata", "06", "La Plata"))));

        assertThat(catalog.provinces()).extracting("id").containsExactly(6L, 86L);
        assertThat(catalog.localities()).extracting("id").containsExactly(6441030L);
        assertThat(catalog.localities().getFirst().getProvince().getId()).isEqualTo(6L);
    }

    @Test
    void nombreUnicoSeMuestraSinDepartamento() {
        GeorefCatalog catalog = GeorefCatalog.from(snapshot(List.of(
                new LocalityRow("06274010", "Florencio Varela", "06", "Florencio Varela"))));

        assertThat(catalog.localities().getFirst().getDisplayName()).isEqualTo("Florencio Varela");
    }

    @Test
    void nombreRepetidoEnLaProvinciaSeMuestraConDepartamento() {
        GeorefCatalog catalog = GeorefCatalog.from(snapshot(List.of(
                new LocalityRow("86063010", "San Pedro", "86", "Choya"),
                new LocalityRow("86098020", "San Pedro", "86", "Jiménez"),
                new LocalityRow("06714010", "San Pedro", "06", "San Pedro"))));

        Map<Long, Locality> localities = byId(catalog);
        assertThat(localities.get(86063010L).getDisplayName()).isEqualTo("San Pedro (Choya)");
        assertThat(localities.get(86098020L).getDisplayName()).isEqualTo("San Pedro (Jiménez)");
        // En Buenos Aires hay un solo San Pedro: no lleva aclaración.
        assertThat(localities.get(6714010L).getDisplayName()).isEqualTo("San Pedro");
    }

    @Test
    void guardaElNombreNormalizadoParaBuscar() {
        GeorefCatalog catalog = GeorefCatalog.from(snapshot(List.of(
                new LocalityRow("06063010", "Bahía Blanca", "06", "Bahía Blanca"))));

        assertThat(catalog.localities().getFirst().getSearchName()).isEqualTo("bahia blanca");
    }

    @Test
    void localidadConProvinciaInexistenteFalla() {
        assertThatThrownBy(() -> GeorefCatalog.from(snapshot(List.of(
                new LocalityRow("99000010", "Ninguna", "99", "Ninguno")))))
                .isInstanceOf(IllegalStateException.class);
    }
}
