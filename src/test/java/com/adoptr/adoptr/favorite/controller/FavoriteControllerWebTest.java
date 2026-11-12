package com.adoptr.adoptr.favorite.controller;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.adoption.repository.AdoptionPublicationRepository;
import com.adoptr.adoptr.adoption.service.impl.AdoptionSummaries;
import com.adoptr.adoptr.favorite.repository.FavoriteRepository;
import com.adoptr.adoptr.favorite.service.impl.FavoriteServiceImpl;
import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.publication.model.PublicationPhoto;
import com.adoptr.adoptr.publication.model.PublicationType;
import com.adoptr.adoptr.publication.repository.PublicationPhotoRepository;
import com.adoptr.adoptr.shared.config.ClockConfig;
import com.adoptr.adoptr.shared.security.JwtAuthFilter;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.shared.security.SecurityConfig;
import com.adoptr.adoptr.shared.storage.ImageStorage;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FavoriteController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, FavoriteServiceImpl.class, AdoptionSummaries.class,
        MembershipSupport.class, FavoriteControllerWebTest.FixedClock.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000"
})
class FavoriteControllerWebTest {

    private static final long AUTHOR_ID = 7L;
    private static final long VIEWER_ID = 8L;

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
    private FavoriteRepository favoriteRepository;

    @MockitoBean
    private AdoptionPublicationRepository adoptionRepository;

    @MockitoBean
    private PublicationPhotoRepository photoRepository;

    @MockitoBean
    private ImageStorage imageStorage;

    @MockitoBean
    private UserRepository userRepository;

    private User author;
    private AdoptionPublication luna;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(AUTHOR_ID);
        User viewer = new User();
        viewer.setId(VIEWER_ID);
        Province province = new Province();
        province.setId(6L);
        province.setName("Buenos Aires");
        Locality locality = new Locality();
        locality.setId(6441030L);
        locality.setName("La Plata");
        locality.setDisplayName("La Plata");
        locality.setProvince(province);
        luna = new AdoptionPublication();
        luna.setId(1L);
        luna.setType(PublicationType.ADOPTION);
        luna.setAuthor(author);
        luna.setLocality(locality);
        luna.setDescription("Muy cariñosa.");
        luna.setCreatedAt(LocalDateTime.of(2026, 10, 1, 9, 30));
        luna.setUpdatedAt(LocalDateTime.of(2026, 10, 1, 9, 30));
        luna.setPetName("Luna");
        luna.setSpecies(Species.DOG);
        luna.setSex(PetSex.FEMALE);
        luna.setSize(PetSize.MEDIUM);
        luna.setEstimatedBirthDate(LocalDate.of(2024, 3, 1));
        luna.setStatus(AdoptionStatus.ADOPTED);
        when(adoptionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(luna));
        when(userRepository.getReferenceById(VIEWER_ID)).thenReturn(viewer);
        when(imageStorage.publicUrl(any())).thenAnswer(invocation -> "https://cdn/" + invocation.getArgument(0));
    }

    private String bearer(long userId) {
        return "Bearer " + jwtUtil.generateToken(userId);
    }

    @Test
    void todasLasRutasRequierenToken() throws Exception {
        mockMvc.perform(get("/favorites/adoptions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mockMvc.perform(get("/favorites/ids"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mockMvc.perform(put("/favorites/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mockMvc.perform(delete("/favorites/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        verifyNoInteractions(favoriteRepository);
    }

    @Test
    void guardarResponde204() throws Exception {
        mockMvc.perform(put("/favorites/1").header("Authorization", bearer(VIEWER_ID)))
                .andExpect(status().isNoContent());

        verify(favoriteRepository).saveAndFlush(any());
    }

    @Test
    void guardarUnaPropiaResponde403() throws Exception {
        mockMvc.perform(put("/favorites/1").header("Authorization", bearer(AUTHOR_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FAVORITE_OWN_PUBLICATION"));
    }

    @Test
    void guardarUnaInexistenteResponde404() throws Exception {
        mockMvc.perform(put("/favorites/99").header("Authorization", bearer(VIEWER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PUBLICATION_NOT_FOUND"));
    }

    @Test
    void quitarResponde204() throws Exception {
        mockMvc.perform(delete("/favorites/1").header("Authorization", bearer(VIEWER_ID)))
                .andExpect(status().isNoContent());

        verify(favoriteRepository).deleteByUserIdAndPublicationId(VIEWER_ID, 1L);
    }

    @Test
    void listaLasTarjetasConPortada() throws Exception {
        PublicationPhoto cover = new PublicationPhoto();
        cover.setPublication(luna);
        cover.setS3Key("adoptions/1/portada.jpg");
        when(favoriteRepository.findAdoptions(eq(VIEWER_ID), eq(Species.DOG), any()))
                .thenReturn(new PageImpl<>(List.of(luna), PageRequest.of(0, 12), 1));
        when(photoRepository.findByPublicationIdInOrderBySortOrderAsc(List.of(1L))).thenReturn(List.of(cover));

        mockMvc.perform(get("/favorites/adoptions?species=DOG").header("Authorization", bearer(VIEWER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].petName").value("Luna"))
                .andExpect(jsonPath("$.items[0].status").value("ADOPTED"))
                .andExpect(jsonPath("$.items[0].coverPhotoUrl").value("https://cdn/adoptions/1/portada.jpg"))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void listarConEspecieInvalidaResponde400() throws Exception {
        mockMvc.perform(get("/favorites/adoptions?species=FISH").header("Authorization", bearer(VIEWER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void idsDevuelveLaLista() throws Exception {
        when(favoriteRepository.findPublicationIds(VIEWER_ID)).thenReturn(List.of(3L, 1L));

        mockMvc.perform(get("/favorites/ids").header("Authorization", bearer(VIEWER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicationIds[0]").value(3))
                .andExpect(jsonPath("$.publicationIds[1]").value(1));
    }
}
