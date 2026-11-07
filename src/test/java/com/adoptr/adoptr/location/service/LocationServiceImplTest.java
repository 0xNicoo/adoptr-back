package com.adoptr.adoptr.location.service;

import com.adoptr.adoptr.location.dto.response.LocalityDTO;
import com.adoptr.adoptr.location.dto.response.ProvinceDTO;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.location.repository.ProvinceRepository;
import com.adoptr.adoptr.location.service.impl.LocationServiceImpl;
import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LocationServiceImplTest {

    private final ProvinceRepository provinceRepository = mock(ProvinceRepository.class);
    private final LocalityRepository localityRepository = mock(LocalityRepository.class);
    private final LocationService service = new LocationServiceImpl(provinceRepository, localityRepository);

    private static Province province(long id, String name) {
        Province province = new Province();
        province.setId(id);
        province.setName(name);
        return province;
    }

    private static Locality locality(long id, String name, String displayName) {
        Locality locality = new Locality();
        locality.setId(id);
        locality.setName(name);
        locality.setDisplayName(displayName);
        locality.setSearchName(name.toLowerCase());
        locality.setDepartmentName("Depto");
        return locality;
    }

    @Test
    void devuelveLasProvinciasComoDto() {
        when(provinceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(province(6, "Buenos Aires"), province(14, "Córdoba")));

        List<ProvinceDTO> provinces = service.getProvinces();

        assertThat(provinces).extracting(ProvinceDTO::getId).containsExactly(6L, 14L);
        assertThat(provinces).extracting(ProvinceDTO::getName).containsExactly("Buenos Aires", "Córdoba");
    }

    @Test
    void buscaConElTextoNormalizadoYDevuelveElNombreParaMostrar() {
        when(provinceRepository.existsById(86L)).thenReturn(true);
        when(localityRepository.search(eq(86L), eq("san pedro"), any(Pageable.class)))
                .thenReturn(List.of(locality(86063010, "San Pedro", "San Pedro (Choya)")));

        List<LocalityDTO> result = service.searchLocalities(86L, "  SAN   Pedro ");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(86063010L);
        assertThat(result.getFirst().getName()).isEqualTo("San Pedro");
        assertThat(result.getFirst().getDisplayName()).isEqualTo("San Pedro (Choya)");
    }

    @Test
    void buscaSinTildesYLimitaA20Resultados() {
        when(provinceRepository.existsById(14L)).thenReturn(true);
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        when(localityRepository.search(eq(14L), eq("rio"), page.capture())).thenReturn(List.of());

        service.searchLocalities(14L, "Río");

        assertThat(page.getValue().getPageSize()).isEqualTo(20);
        assertThat(page.getValue().getPageNumber()).isZero();
    }

    @Test
    void escapaLosComodinesDelLike() {
        when(provinceRepository.existsById(6L)).thenReturn(true);

        service.searchLocalities(6L, "a%_b");

        verify(localityRepository).search(eq(6L), eq("a\\%\\_b"), any(Pageable.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"a", "  á  "})
    void busquedaDeMenosDeDosLetrasEsInvalida(String query) {
        assertThatThrownBy(() -> service.searchLocalities(6L, query))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", "VALIDATION_ERROR");

        verifyNoInteractions(localityRepository);
        verify(provinceRepository, never()).existsById(anyLong());
    }

    @Test
    void busquedaDemasiadoLargaEsInvalida() {
        assertThatThrownBy(() -> service.searchLocalities(6L, "a".repeat(101)))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(localityRepository);
    }

    @Test
    void provinciaInexistenteDevuelveNotFound() {
        when(provinceRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.searchLocalities(99L, "la plata"))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "PROVINCE_NOT_FOUND");

        verifyNoInteractions(localityRepository);
    }
}
