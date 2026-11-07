package com.adoptr.adoptr.shared.security;

import com.adoptr.adoptr.auth.controller.AuthController;
import com.adoptr.adoptr.auth.service.AuthService;
import com.adoptr.adoptr.profile.controller.ProfileController;
import com.adoptr.adoptr.location.repository.LocalityRepository;
import com.adoptr.adoptr.profile.model.PersonProfile;
import com.adoptr.adoptr.profile.repository.ProfileRepository;
import com.adoptr.adoptr.profile.service.impl.ProfileServiceImpl;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.repository.UserRepository;
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
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la cadena HTTP completa (filtro JWT, reglas de acceso, handlers de error)
 * sin base de datos: el repositorio de perfiles y el servicio de login están simulados.
 */
@WebMvcTest(controllers = {ProfileController.class, AuthController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, ProfileServiceImpl.class, MembershipSupport.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000"
})
class SecurityAndErrorsWebTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123456";
    private static final String OTHER_SECRET = "another-secret-another-secret-another-9999";

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

    @MockitoBean
    private AuthService authService;

    private String bearer(Long userId) {
        return "Bearer " + jwtUtil.generateToken(userId);
    }

    // ---- Autenticación: 401 con formato ApiError ----

    @Test
    void sinTokenDevuelve401ConFormatoDeError() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verifyNoInteractions(profileRepository);
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/profile").header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void tokenVencidoDevuelve401() throws Exception {
        String expired = new JwtUtil(SECRET, -1_000).generateToken(7L);

        mockMvc.perform(get("/profile").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFirmadoConOtroSecretoDevuelve401() throws Exception {
        String forged = new JwtUtil(OTHER_SECRET, 3_600_000).generateToken(7L);

        mockMvc.perform(get("/profile").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    // ---- Principal = id interno del usuario ----

    @Test
    void tokenValidoBuscaElPerfilDelUsuarioDelToken() throws Exception {
        PersonProfile profile = new PersonProfile();
        profile.setId(1L);
        profile.setFirstName("Dolores");
        profile.setLastName("Dominguez");
        when(profileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/profile").header("Authorization", bearer(7L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Dolores"))
                .andExpect(jsonPath("$.lastName").value("Dominguez"));

        verify(profileRepository).findByUserId(7L);
    }

    @Test
    void tokenValidoSinPerfilDevuelve404() throws Exception {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/profile").header("Authorization", bearer(7L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ---- Errores sin filtrar detalles internos ----

    @Test
    void unErrorInesperadoDevuelve500SinMensajeInterno() throws Exception {
        when(profileRepository.findByUserId(anyLong()))
                .thenThrow(new RuntimeException("password=hunter2 jdbc:postgresql://db:5432/adoptr"));

        mockMvc.perform(get("/profile").header("Authorization", bearer(7L)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(not(containsString("hunter2"))))
                .andExpect(content().string(not(containsString("jdbc"))));
    }

    @Test
    void rutaInexistenteAutenticadaDevuelve404YNoEs500() throws Exception {
        mockMvc.perform(get("/no-existe").header("Authorization", bearer(7L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROUTE_NOT_FOUND"));
    }

    @Test
    void metodoNoPermitidoDevuelve405() throws Exception {
        mockMvc.perform(delete("/profile").header("Authorization", bearer(7L)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    // ---- /auth/oauth: público pero validado ----

    @Test
    void loginEsPublicoYConBodyVacioDevuelve400YNoEs500() throws Exception {
        mockMvc.perform(post("/auth/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message", containsString("provider")))
                .andExpect(jsonPath("$.message", containsString("token")));

        verifyNoInteractions(authService);
    }

    @Test
    void loginConJsonMalformadoDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        verifyNoInteractions(authService);
    }
}
