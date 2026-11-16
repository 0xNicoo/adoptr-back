package com.adoptr.adoptr.user.controller;

import com.adoptr.adoptr.shared.security.JwtAuthFilter;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.shared.security.SecurityConfig;
import com.adoptr.adoptr.shared.util.MembershipSupport;
import com.adoptr.adoptr.user.model.User;
import com.adoptr.adoptr.user.repository.UserRepository;
import com.adoptr.adoptr.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, UserServiceImpl.class, MembershipSupport.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000"
})
class UserControllerWebTest {

    private static final long USER_ID = 7L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    private final User user = new User();

    @BeforeEach
    void setUp() {
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private String bearer() {
        return "Bearer " + jwtUtil.generateToken(USER_ID);
    }

    @Test
    void sinTokenDa401() throws Exception {
        mockMvc.perform(get("/user/google-photo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devuelveLaFotoDeGoogleDelUsuarioDelToken() throws Exception {
        user.setGooglePhotoUrl("https://lh3.googleusercontent.com/a/foto=s96-c");

        mockMvc.perform(get("/user/google-photo").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://lh3.googleusercontent.com/a/foto=s96-c"));
    }

    @Test
    void sinFotoDeGoogleDevuelveUrlNull() throws Exception {
        mockMvc.perform(get("/user/google-photo").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(nullValue()));
    }
}
