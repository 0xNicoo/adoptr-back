package com.adoptr.adoptr.profile.controller;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.profile.model.Gender;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.repository.ProfilePhotoRepository;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.profile.service.impl.ProfileServiceImpl;
import com.adoptr.adoptr.shared.config.ClockConfig;
import com.adoptr.adoptr.shared.security.JwtAuthFilter;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.shared.security.SecurityConfig;
import com.adoptr.adoptr.shared.storage.ImageProcessor;
import com.adoptr.adoptr.shared.storage.ImageStorage;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba GET /profiles/{id} (público) con la cadena de seguridad real y los repositorios simulados. */
@WebMvcTest(controllers = PublicProfileController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, ProfileServiceImpl.class, MembershipSupport.class,
        ClockConfig.class, ImageProcessor.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000"
})
class PublicProfileControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private ProfilePhotoRepository profilePhotoRepository;

    @MockitoBean
    private ImageStorage imageStorage;

    @MockitoBean
    private LocalityRepository localityRepository;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        Province province = new Province();
        province.setId(6L);
        province.setName("Buenos Aires");
        Locality locality = new Locality();
        locality.setId(6441030L);
        locality.setName("La Plata");
        locality.setDisplayName("La Plata");
        locality.setProvince(province);

        User user = new User();
        user.setId(7L);
        user.setEmail("secreto@example.com");
        user.setProviderUserId("google-123");

        PersonProfile profile = new PersonProfile();
        profile.setId(3L);
        profile.setUser(user);
        profile.setFirstName("Victoria");
        profile.setLastName("Miranda");
        profile.setGenderType(Gender.FEMALE);
        profile.setDescription("Doy tránsito.");
        profile.setLocality(locality);
        when(profileRepository.findById(3L)).thenReturn(Optional.of(profile));
        when(profilePhotoRepository.findByProfileId(3L)).thenReturn(Optional.empty());
    }

    @Test
    void sinTokenDevuelveElPerfilConDescripcionYLocalidad() throws Exception {
        mockMvc.perform(get("/profiles/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.profileType").value("PERSON"))
                .andExpect(jsonPath("$.firstName").value("Victoria"))
                .andExpect(jsonPath("$.lastName").value("Miranda"))
                .andExpect(jsonPath("$.description").value("Doy tránsito."))
                .andExpect(jsonPath("$.locality.displayName").value("La Plata"))
                .andExpect(jsonPath("$.province.name").value("Buenos Aires"))
                .andExpect(jsonPath("$.photoUrl").isEmpty());
    }

    @Test
    void noExponeGeneroNiDatosDelUsuario() throws Exception {
        mockMvc.perform(get("/profiles/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genderType").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$..email").isEmpty())
                .andExpect(jsonPath("$..providerUserId").isEmpty());
    }

    @Test
    void conTokenInvalidoSiguePublico() throws Exception {
        mockMvc.perform(get("/profiles/3").header("Authorization", "Bearer basura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Victoria"));
    }

    @Test
    void inexistenteDevuelve404() throws Exception {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/profiles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_FOUND"));
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/profiles/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void otrosMetodosSiguenExigiendoToken() throws Exception {
        mockMvc.perform(post("/profiles/3"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
}
