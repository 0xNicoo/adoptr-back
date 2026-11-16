package com.adoptr.adoptr.user.google;

import com.adoptr.adoptr.shared.exception.custom.ServiceUnavailableException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GooglePhotoClientTest {

    private HttpServer server;
    private final GooglePhotoClient client = new GooglePhotoClient(Duration.ofMillis(500), 10);

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private URI respond(String path, int status, byte[] body) {
        server.createContext(path, exchange -> {
            exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://lh3.googleusercontent.com/a/ACg8ocK=s96-c",
            "https://LH4.GoogleUserContent.com/a/foto"
    })
    void aceptaFotosDeGoogle(String url) {
        assertThat(GooglePhotoClient.isAllowed(url)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "",
            "http://lh3.googleusercontent.com/a/foto",
            "https://googleusercontent.com/a/foto",
            "https://lh3.googleusercontent.com.evil.com/a/foto",
            "https://evil.com/lh3.googleusercontent.com",
            "https://user@lh3.googleusercontent.com/a/foto",
            "https://lh3.googleusercontent.com:8443/a/foto",
            "https://127.0.0.1/a/foto",
            "no es una url"
    })
    void rechazaCualquierOtraUrl(String url) {
        assertThat(GooglePhotoClient.isAllowed(url)).isFalse();
    }

    @Test
    void pideLaFotoEnUnTamanoMasGrande() {
        assertThat(GooglePhotoClient.sized("https://lh3.googleusercontent.com/a/ACg8ocK=s96-c"))
                .isEqualTo("https://lh3.googleusercontent.com/a/ACg8ocK=s800-c");
        assertThat(GooglePhotoClient.sized("https://lh3.googleusercontent.com/a/ACg8ocK=s96"))
                .isEqualTo("https://lh3.googleusercontent.com/a/ACg8ocK=s800-c");
    }

    @Test
    void sinSufijoDeTamanoDejaLaUrlComoEsta() {
        assertThat(GooglePhotoClient.sized("https://lh3.googleusercontent.com/a/foto"))
                .isEqualTo("https://lh3.googleusercontent.com/a/foto");
    }

    @Test
    void descargarUnaUrlNoPermitidaDa503SinPedirNada() {
        assertThatThrownBy(() -> client.download("https://evil.com/foto.jpg"))
                .isInstanceOfSatisfying(ServiceUnavailableException.class,
                        e -> assertThat(e.getCode()).isEqualTo("GOOGLE_PHOTO_UNAVAILABLE"));
    }

    @Test
    void traeLosBytesDeLaFoto() {
        URI uri = respond("/ok", 200, new byte[]{1, 2, 3});

        assertThat(client.fetch(uri)).containsExactly(1, 2, 3);
    }

    @Test
    void unaRespuestaQueNoEs200Da503() {
        URI uri = respond("/missing", 404, new byte[]{1});

        assertThatThrownBy(() -> client.fetch(uri))
                .isInstanceOfSatisfying(ServiceUnavailableException.class,
                        e -> assertThat(e.getCode()).isEqualTo("GOOGLE_PHOTO_UNAVAILABLE"));
    }

    @Test
    void noSigueRedirecciones() {
        server.createContext("/redirect", exchange -> {
            exchange.getResponseHeaders().add("Location", "https://evil.com/foto.jpg");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/redirect");

        assertThatThrownBy(() -> client.fetch(uri)).isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void unaFotoMasGrandeQueElLimiteDa503() {
        URI uri = respond("/big", 200, new byte[11]);

        assertThatThrownBy(() -> client.fetch(uri)).isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void unaRespuestaVaciaDa503() {
        URI uri = respond("/empty", 200, new byte[0]);

        assertThatThrownBy(() -> client.fetch(uri)).isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void siGoogleNoRespondeATiempoDa503() {
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });
        URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/slow");

        assertThatThrownBy(() -> client.fetch(uri)).isInstanceOf(ServiceUnavailableException.class);
    }
}
