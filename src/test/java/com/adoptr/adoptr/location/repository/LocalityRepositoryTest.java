package com.adoptr.adoptr.location.repository;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.shared.util.SearchText;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la consulta de búsqueda contra PostgreSQL real. Como AdoptrApplicationTests,
 * necesita la base y las variables de entorno. Usa ids fuera del rango de Georef y
 * cada test se revierte al terminar.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LocalityRepositoryTest {

    private static final long PROVINCE_ID = 999L;
    private static final long OTHER_PROVINCE_ID = 998L;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private LocalityRepository localityRepository;

    private long nextId = 999_000_001L;

    private void locality(Province province, String name) {
        Locality locality = new Locality();
        locality.setId(nextId++);
        locality.setName(name);
        locality.setDisplayName(name);
        locality.setSearchName(SearchText.normalize(name));
        locality.setDepartmentName("Test");
        locality.setProvince(province);
        localityRepository.save(locality);
    }

    @BeforeEach
    void setUp() {
        Province province = new Province();
        province.setId(PROVINCE_ID);
        province.setName("Provincia de prueba");
        provinceRepository.save(province);
        Province other = new Province();
        other.setId(OTHER_PROVINCE_ID);
        other.setName("Otra provincia de prueba");
        provinceRepository.save(other);

        locality(province, "Villa Río Cuarto");
        locality(province, "Río Cuarto");
        locality(province, "Río Ceballos");
        locality(province, "Córdoba");
        locality(province, "100% Pueblo");
        locality(other, "Río Tercero");
        localityRepository.flush();
    }

    private List<String> search(long provinceId, String text) {
        return localityRepository.search(provinceId, text, PageRequest.of(0, 20)).stream()
                .map(Locality::getName)
                .toList();
    }

    @Test
    void primeroLasQueEmpiezanConElTextoYSoloDeEsaProvincia() {
        assertThat(search(PROVINCE_ID, "rio"))
                .containsExactly("Río Ceballos", "Río Cuarto", "Villa Río Cuarto");
    }

    @Test
    void respetaElLimiteDeResultados() {
        assertThat(localityRepository.search(PROVINCE_ID, "rio", PageRequest.of(0, 2))).hasSize(2);
    }

    @Test
    void elPorcentajeEscapadoSeBuscaLiteral() {
        assertThat(search(PROVINCE_ID, "100\\%")).containsExactly("100% Pueblo");
        assertThat(search(PROVINCE_ID, "\\%")).containsExactly("100% Pueblo");
    }
}
