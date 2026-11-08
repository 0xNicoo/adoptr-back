package com.adoptr.adoptr.adoption.controller;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.adoption.service.impl.AdoptionServiceImpl;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.profile.model.OrganizationProfile;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.shared.config.ClockConfig;
import com.adoptr.adoptr.shared.security.JwtAuthFilter;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.shared.security.SecurityConfig;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba /adoptions con la cadena de seguridad real, el servicio real y los repositorios simulados. */
@WebMvcTest(controllers = AdoptionController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, AdoptionServiceImpl.class,
        MembershipSupport.class, AdoptionControllerWebTest.FixedClock.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000"
})
class AdoptionControllerWebTest {

    private static final long OWNER_ID = 7L;
    private static final long OTHER_ID = 8L;
    private static final long LOCALITY_ID = 6441030L;

    private static final String DOG_JSON = """
            {
              "petName": "Luna",
              "description": "Muy cariñosa.",
              "species": "DOG",
              "sex": "FEMALE",
              "size": "MEDIUM",
              "estimatedBirthDate": "2024-03",
              "vaccinated": true,
              "dewormed": true,
              "neutered": false,
              "localityId": 6441030
            }
            """;

    @TestConfiguration
    static class FixedClock {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-11-07T15:00:00Z"), ClockConfig.ZONE);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private AdoptionPublicationRepository adoptionRepository;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private LocalityRepository localityRepository;

    @MockitoBean
    private UserRepository userRepository;

    private User owner;
    private Locality locality;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(OWNER_ID);
        owner.setEmail("secreto@example.com");
        owner.setProviderUserId("google-123");
        Province province = new Province();
        province.setId(6L);
        province.setName("Buenos Aires");
        locality = new Locality();
        locality.setId(LOCALITY_ID);
        locality.setName("La Plata");
        locality.setDisplayName("La Plata");
        locality.setProvince(province);
        OrganizationProfile organization = new OrganizationProfile();
        organization.setOrganizationName("Refugio Patitas");
        when(profileRepository.findByUserId(OWNER_ID)).thenReturn(Optional.of(organization));
        when(profileRepository.existsByUserId(OWNER_ID)).thenReturn(true);
        when(localityRepository.findById(LOCALITY_ID)).thenReturn(Optional.of(locality));
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(owner);
        when(adoptionRepository.save(any(AdoptionPublication.class))).thenAnswer(invocation -> {
            AdoptionPublication p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            return p;
        });
    }

    private String bearer(long userId) {
        return "Bearer " + jwtUtil.generateToken(userId);
    }

    private AdoptionPublication existing() {
        AdoptionPublication p = new AdoptionPublication();
        p.setId(1L);
        p.setType(PublicationType.ADOPTION);
        p.setAuthor(owner);
        p.setLocality(locality);
        p.setDescription("Muy cariñosa.");
        p.setCreatedAt(LocalDateTime.of(2026, 10, 1, 9, 30));
        p.setUpdatedAt(LocalDateTime.of(2026, 10, 1, 9, 30));
        p.setPetName("Luna");
        p.setSpecies(Species.DOG);
        p.setSex(PetSex.FEMALE);
        p.setSize(PetSize.MEDIUM);
        p.setEstimatedBirthDate(LocalDate.of(2024, 3, 1));
        p.setVaccinated(true);
        p.setDewormed(true);
        p.setNeutered(false);
        p.setStatus(AdoptionStatus.AVAILABLE);
        when(adoptionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(p));
        return p;
    }

    // ---- Detalle público ----

    @Test
    void detalleSinTokenEsPublicoYNoEsDueno() throws Exception {
        existing();

        mockMvc.perform(get("/adoptions/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "private"))
                .andExpect(jsonPath("$.viewerIsOwner").value(false))
                .andExpect(jsonPath("$.petName").value("Luna"));
    }

    @Test
    void detalleConTokenInvalidoEsPublicoYNoEsDueno() throws Exception {
        existing();

        mockMvc.perform(get("/adoptions/1").header("Authorization", "Bearer basura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewerIsOwner").value(false));
    }

    @Test
    void detalleConTokenDelDuenoEsDueno() throws Exception {
        existing();

        mockMvc.perform(get("/adoptions/1").header("Authorization", bearer(OWNER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewerIsOwner").value(true));
    }

    @Test
    void detalleNoExponeDatosPrivados() throws Exception {
        existing();

        mockMvc.perform(get("/adoptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estimatedBirthDate").value("2024-03"))
                .andExpect(jsonPath("$.lifeStage").value("ADULT"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.size").value("MEDIUM"))
                .andExpect(jsonPath("$.vaccinated").value(true))
                .andExpect(jsonPath("$.locality.displayName").value("La Plata"))
                .andExpect(jsonPath("$.province.name").value("Buenos Aires"))
                .andExpect(jsonPath("$.owner.displayName").value("Refugio Patitas"))
                .andExpect(jsonPath("$.owner.profileType").value("ORGANIZATION"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-01T09:30:00"))
                .andExpect(jsonPath("$.author").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$..email").isEmpty())
                .andExpect(jsonPath("$..providerUserId").isEmpty())
                .andExpect(jsonPath("$.deletedAt").doesNotExist());
    }

    @Test
    void detalleInexistenteDevuelve404AdoptionNotFound() throws Exception {
        when(adoptionRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/adoptions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADOPTION_NOT_FOUND"));
    }

    // ---- Mis publicaciones ----

    @Test
    void mineSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/adoptions/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        verifyNoInteractions(adoptionRepository);
    }

    @Test
    void mineConTokenDevuelvePagina() throws Exception {
        AdoptionPublication p = existing();
        when(adoptionRepository.findByAuthorIdAndDeletedAtIsNull(eq(OWNER_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/adoptions/mine").header("Authorization", bearer(OWNER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].petName").value("Luna"))
                .andExpect(jsonPath("$.items[0].description").doesNotExist())
                .andExpect(jsonPath("$.items[0].owner").doesNotExist())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void mineConPageSize51Devuelve400() throws Exception {
        mockMvc.perform(get("/adoptions/mine").param("pageSize", "51").header("Authorization", bearer(OWNER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // ---- Alta ----

    @Test
    void crearSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/adoptions").contentType(MediaType.APPLICATION_JSON).content(DOG_JSON))
                .andExpect(status().isUnauthorized());

        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void crearDevuelve201() throws Exception {
        mockMvc.perform(post("/adoptions").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(DOG_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.estimatedBirthDate").value("2024-03"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-07T12:00:00"))
                .andExpect(jsonPath("$.viewerIsOwner").value(true));
    }

    @Test
    void crearSinPerfilDevuelve403ProfileRequired() throws Exception {
        when(profileRepository.existsByUserId(OTHER_ID)).thenReturn(false);

        mockMvc.perform(post("/adoptions").header("Authorization", bearer(OTHER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(DOG_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROFILE_REQUIRED"));
    }

    @Test
    void crearSinVacunadoDevuelve400ValidationError() throws Exception {
        mockMvc.perform(post("/adoptions").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOG_JSON.replace("\"vaccinated\": true,", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void crearConNombreEnBlancoDevuelve400() throws Exception {
        mockMvc.perform(post("/adoptions").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOG_JSON.replace("\"Luna\"", "\"   \"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void crearConFechaMalFormadaDevuelveMalformedRequest() throws Exception {
        mockMvc.perform(post("/adoptions").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOG_JSON.replace("2024-03", "2024-13")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void crearConEspecieInvalidaDevuelveMalformedRequest() throws Exception {
        mockMvc.perform(post("/adoptions").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOG_JSON.replace("\"DOG\"", "\"FISH\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    // ---- Edición, estado y baja ----

    @Test
    void editarAjenaDevuelve403AdoptionNotOwner() throws Exception {
        existing();

        mockMvc.perform(put("/adoptions/1").header("Authorization", bearer(OTHER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(DOG_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ADOPTION_NOT_OWNER"));
    }

    @Test
    void editarPorDuenoDevuelve200() throws Exception {
        existing();

        mockMvc.perform(put("/adoptions/1").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content(DOG_JSON.replace("Luna", "Lunita")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.petName").value("Lunita"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-07T12:00:00"));
    }

    @Test
    void editarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(put("/adoptions/1").contentType(MediaType.APPLICATION_JSON).content(DOG_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cambiarEstadoDevuelve200() throws Exception {
        existing();

        mockMvc.perform(patch("/adoptions/1/status").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"ADOPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADOPTED"));
    }

    @Test
    void cambiarEstadoSinEstadoDevuelve400() throws Exception {
        existing();

        mockMvc.perform(patch("/adoptions/1/status").header("Authorization", bearer(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void bajaDevuelve204() throws Exception {
        existing();

        mockMvc.perform(delete("/adoptions/1").header("Authorization", bearer(OWNER_ID)))
                .andExpect(status().isNoContent());
    }

    @Test
    void bajaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(delete("/adoptions/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adoptionRepository);
    }

    // ---- Catálogo público ----

    @Test
    @SuppressWarnings("unchecked")
    void catalogoSinTokenDevuelve200() throws Exception {
        AdoptionPublication p = existing();
        when(adoptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/adoptions").param("species", "DOG").param("lifeStage", "ADULT").param("provinceId", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].petName").value("Luna"))
                .andExpect(jsonPath("$.items[0].description").doesNotExist())
                .andExpect(jsonPath("$.items[0].owner").doesNotExist())
                .andExpect(jsonPath("$.items[0].viewerIsOwner").doesNotExist())
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void catalogoConTokenInvalidoSiguePublico() throws Exception {
        when(adoptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/adoptions").header("Authorization", "Bearer basura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void catalogoConEspecieInvalidaDevuelve400ValidationError() throws Exception {
        mockMvc.perform(get("/adoptions").param("species", "PEZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Los datos enviados no son válidos (species: formato inválido)"));

        verifyNoInteractions(adoptionRepository);
    }

    @Test
    void catalogoConProvinciaNoNumericaNoExponeClasesInternas() throws Exception {
        mockMvc.perform(get("/adoptions").param("provinceId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(
                        "Los datos enviados no son válidos (provinceId: formato inválido)"));
    }

    @Test
    void catalogoConPageSize51Devuelve400() throws Exception {
        mockMvc.perform(get("/adoptions").param("pageSize", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void mineSigueRequiriendoToken() throws Exception {
        mockMvc.perform(get("/adoptions/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicarSigueRequiriendoToken() throws Exception {
        mockMvc.perform(post("/adoptions").contentType(MediaType.APPLICATION_JSON).content(DOG_JSON))
                .andExpect(status().isUnauthorized());
    }
}
