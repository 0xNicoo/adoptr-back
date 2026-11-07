package com.adoptr.adoptr.profile.controller;

import com.adoptr.adoptr.location.model.Locality;
import com.adoptr.adoptr.location.model.Province;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.model.Profile;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.profile.service.impl.ProfileServiceImpl;
import com.adoptr.adoptr.shared.security.JwtAuthFilter;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.shared.security.SecurityConfig;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba /profile con la cadena de seguridad real y los repositorios simulados. */
@WebMvcTest(controllers = ProfileController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, ProfileServiceImpl.class, MembershipSupport.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000"
})
class ProfileControllerWebTest {

    private static final long USER_ID = 7L;

    private static final String PERSON_JSON = """
            {
              "profileType": "PERSON",
              "firstName": "Dolores",
              "lastName": "Dominguez",
              "genderType": "X",
              "description": "Tengo dos gatos.",
              "localityId": 6441030
            }
            """;

    private static final String ORGANIZATION_JSON = """
            {
              "profileType": "ORGANIZATION",
              "organizationName": "Refugio Patitas",
              "instagram": "@refugiopatitas",
              "website": "https://patitas.org",
              "description": "Rescatamos perros y gatos.",
              "localityId": 6441030
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private LocalityRepository localityRepository;

    @MockitoBean
    private UserRepository userRepository;

    private String bearer() {
        return "Bearer " + jwtUtil.generateToken(USER_ID);
    }

    private static Locality laPlata() {
        Province province = new Province();
        province.setId(6L);
        province.setName("Buenos Aires");
        Locality locality = new Locality();
        locality.setId(6441030L);
        locality.setName("La Plata");
        locality.setDisplayName("La Plata");
        locality.setSearchName("la plata");
        locality.setDepartmentName("La Plata");
        locality.setProvince(province);
        return locality;
    }

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(localityRepository.findById(6441030L)).thenReturn(Optional.of(laPlata()));
        when(profileRepository.saveAndFlush(any(Profile.class))).thenAnswer(invocation -> {
            Profile saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
    }

    @Test
    void altaDePersonaDevuelve201ConElPerfil() throws Exception {
        mockMvc.perform(post("/profile").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(PERSON_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.profileType").value("PERSON"))
                .andExpect(jsonPath("$.firstName").value("Dolores"))
                .andExpect(jsonPath("$.genderType").value("X"))
                .andExpect(jsonPath("$.locality.id").value(6441030))
                .andExpect(jsonPath("$.locality.displayName").value("La Plata"))
                .andExpect(jsonPath("$.locality.searchName").doesNotExist())
                .andExpect(jsonPath("$.province.name").value("Buenos Aires"))
                .andExpect(jsonPath("$.organizationName").isEmpty());
    }

    @Test
    void altaDeOrganizacionDevuelve201ConElPerfil() throws Exception {
        mockMvc.perform(post("/profile").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(ORGANIZATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileType").value("ORGANIZATION"))
                .andExpect(jsonPath("$.organizationName").value("Refugio Patitas"))
                .andExpect(jsonPath("$.instagram").value("@refugiopatitas"))
                .andExpect(jsonPath("$.firstName").isEmpty());
    }

    @Test
    void altaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/profile").contentType(MediaType.APPLICATION_JSON).content(PERSON_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        verifyNoInteractions(profileRepository);
    }

    @Test
    void altaSinCamposComunesDevuelve400ConLosCampos() throws Exception {
        mockMvc.perform(post("/profile").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message", containsString("profileType")))
                .andExpect(jsonPath("$.message", containsString("description")))
                .andExpect(jsonPath("$.message", containsString("localityId")));

        verifyNoInteractions(profileRepository);
    }

    @Test
    void altaDePersonaSinGeneroDevuelve400() throws Exception {
        String json = PERSON_JSON.replace("\"genderType\": \"X\",", "");

        mockMvc.perform(post("/profile").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message", containsString("genderType")));
    }

    @Test
    void tipoDePerfilInexistenteDevuelve400() throws Exception {
        String json = PERSON_JSON.replace("\"PERSON\"", "\"REFUGIO\"");

        mockMvc.perform(post("/profile").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void descripcionDeMasDe500CaracteresDevuelve400() throws Exception {
        String json = PERSON_JSON.replace("Tengo dos gatos.", "a".repeat(501));

        mockMvc.perform(post("/profile").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("description")));
    }

    @Test
    void localidadInexistenteDevuelve400() throws Exception {
        String json = PERSON_JSON.replace("6441030", "1");

        mockMvc.perform(post("/profile").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCALITY_NOT_FOUND"));

        verify(profileRepository, never()).saveAndFlush(any());
    }

    @Test
    void siYaTienePerfilDevuelve409() throws Exception {
        when(profileRepository.existsByUserId(USER_ID)).thenReturn(true);

        mockMvc.perform(post("/profile").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(PERSON_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROFILE_ALREADY_EXISTS"));

        verify(profileRepository, never()).saveAndFlush(any());
    }

    @Test
    void getDevuelveElPerfilConLaNuevaForma() throws Exception {
        PersonProfile profile = new PersonProfile();
        profile.setId(1L);
        profile.setFirstName("Dolores");
        profile.setLastName("Dominguez");
        profile.setDescription("Tengo dos gatos.");
        profile.setLocality(laPlata());
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/profile").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileType").value("PERSON"))
                .andExpect(jsonPath("$.locality.name").value("La Plata"))
                .andExpect(jsonPath("$.locality.province").doesNotExist())
                .andExpect(jsonPath("$.province.id").value(6));
    }
}
