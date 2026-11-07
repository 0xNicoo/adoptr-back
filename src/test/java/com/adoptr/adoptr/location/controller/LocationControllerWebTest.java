package com.adoptr.adoptr.location.controller;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.location.repository.ProvinceRepository;
import com.adoptr.adoptr.location.service.impl.LocationServiceImpl;
import com.adoptr.adoptr.shared.security.JwtAuthFilter;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba los endpoints de ubicaciones con la cadena de seguridad real y los repositorios simulados. */
@WebMvcTest(controllers = LocationController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, LocationServiceImpl.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000"
})
class LocationControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProvinceRepository provinceRepository;

    @MockitoBean
    private LocalityRepository localityRepository;

    private String bearer() {
        return "Bearer " + jwtUtil.generateToken(7L);
    }

    private static Province province(long id, String name) {
        Province province = new Province();
        province.setId(id);
        province.setName(name);
        return province;
    }

    @Test
    void provinciasSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/provinces"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        verifyNoInteractions(provinceRepository);
    }

    @Test
    void localidadesSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/provinces/6/localities").param("q", "la plata"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(localityRepository);
    }

    @Test
    void listaLasProvincias() throws Exception {
        when(provinceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(province(6, "Buenos Aires"), province(2, "Ciudad Autónoma de Buenos Aires")));

        mockMvc.perform(get("/provinces").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(6))
                .andExpect(jsonPath("$[0].name").value("Buenos Aires"));
    }

    @Test
    void buscaLocalidadesYDevuelveSoloElContratoPublico() throws Exception {
        Locality locality = new Locality();
        locality.setId(86063010L);
        locality.setName("San Pedro");
        locality.setDisplayName("San Pedro (Choya)");
        locality.setSearchName("san pedro");
        locality.setDepartmentName("Choya");
        locality.setProvince(province(86, "Santiago del Estero"));
        when(provinceRepository.existsById(86L)).thenReturn(true);
        when(localityRepository.search(eq(86L), eq("san pedro"), any(Pageable.class))).thenReturn(List.of(locality));

        mockMvc.perform(get("/provinces/86/localities").param("q", "San Pedro").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(86063010L))
                .andExpect(jsonPath("$[0].name").value("San Pedro"))
                .andExpect(jsonPath("$[0].displayName").value("San Pedro (Choya)"))
                .andExpect(jsonPath("$[0].searchName").doesNotExist())
                .andExpect(jsonPath("$[0].province").doesNotExist());
    }

    @Test
    void busquedaCortaDevuelve400() throws Exception {
        mockMvc.perform(get("/provinces/6/localities").param("q", "a").header("Authorization", bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(localityRepository);
    }

    @Test
    void busquedaSinTextoDevuelve400() throws Exception {
        mockMvc.perform(get("/provinces/6/localities").header("Authorization", bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void provinciaInexistenteDevuelve404() throws Exception {
        when(provinceRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(get("/provinces/99/localities").param("q", "la plata").header("Authorization", bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROVINCE_NOT_FOUND"));
    }

    @Test
    void idDeProvinciaNoNumericoDevuelve400YNoEs500() throws Exception {
        mockMvc.perform(get("/provinces/abc/localities").param("q", "la plata").header("Authorization", bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
