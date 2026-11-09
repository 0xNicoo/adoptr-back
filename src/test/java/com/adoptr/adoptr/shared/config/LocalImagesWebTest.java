package com.adoptr.adoptr.shared.config;

import com.adoptr.adoptr.auth.controller.AuthController;
import com.adoptr.adoptr.auth.service.AuthService;
import com.adoptr.adoptr.shared.security.JwtAuthFilter;
import com.adoptr.adoptr.shared.security.JwtUtil;
import com.adoptr.adoptr.shared.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Con la seguridad real: las imágenes del modo local se leen sin token, como las del bucket público. */
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, LocalImagesWebConfig.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-test-secret-test-secret-123456",
        "security.jwt.expiration=3600000",
        "app.images.storage=local"
})
class LocalImagesWebTest {

    private static final byte[] IMAGE = {1, 2, 3};
    private static final Path ROOT = createRoot();

    private static Path createRoot() {
        try {
            Path root = Files.createTempDirectory("local-images");
            Files.createDirectories(root.resolve("adoptions/1"));
            Files.write(root.resolve("adoptions/1/a.jpg"), IMAGE);
            return root;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void localDir(DynamicPropertyRegistry registry) {
        registry.add("app.images.local.dir", ROOT::toString);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void unAnonimoLeeLaImagen() throws Exception {
        mockMvc.perform(get("/local-images/adoptions/1/a.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(IMAGE));
    }

    @Test
    void unaImagenQueNoExisteResponde404() throws Exception {
        mockMvc.perform(get("/local-images/adoptions/1/otra.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void noSeSaleDeLaCarpeta() throws Exception {
        Files.writeString(ROOT.resolveSibling("secreto.txt"), "no debería leerse");

        mockMvc.perform(get("/local-images/%2e%2e/secreto.txt"))
                .andExpect(status().is4xxClientError());
    }
}
