package com.adoptr.adoptr.shared.storage;

import com.adoptr.adoptr.shared.exception.custom.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalImageStorageTest {

    private static final ProcessedImage IMAGE = new ProcessedImage(new byte[]{1, 2, 3}, "image/jpeg", "jpg");

    @TempDir
    private Path root;

    private LocalImageStorage storage() {
        return new LocalImageStorage(root.toString(), "http://localhost:8081/local-images/");
    }

    @Test
    void guardaLaImagenCreandoLasCarpetasDeLaKey() throws IOException {
        storage().upload("adoptions/1/a.jpg", IMAGE);

        assertThat(Files.readAllBytes(root.resolve("adoptions/1/a.jpg"))).isEqualTo(IMAGE.bytes());
    }

    @Test
    void siNoSePuedeGuardarResponde503() throws IOException {
        Files.writeString(root.resolve("adoptions"), "un archivo donde debería ir la carpeta");

        assertThatThrownBy(() -> storage().upload("adoptions/1/a.jpg", IMAGE))
                .isInstanceOfSatisfying(ServiceUnavailableException.class,
                        e -> assertThat(e.getCode()).isEqualTo("IMAGE_STORAGE_UNAVAILABLE"));
    }

    @Test
    void borraLaImagen() {
        storage().upload("adoptions/1/a.jpg", IMAGE);

        storage().delete("adoptions/1/a.jpg");

        assertThat(root.resolve("adoptions/1/a.jpg")).doesNotExist();
    }

    @Test
    void borrarUnaImagenQueNoExisteNoFalla() {
        assertThatCode(() -> storage().delete("adoptions/1/a.jpg")).doesNotThrowAnyException();
    }

    @Test
    void unBorradoFallidoNoInterrumpeLaOperacion() {
        storage().upload("adoptions/1/a.jpg", IMAGE);

        // Una carpeta con contenido no se puede borrar.
        assertThatCode(() -> storage().delete("adoptions")).doesNotThrowAnyException();
    }

    @Test
    void armaLaUrlSinDuplicarLaBarra() {
        assertThat(storage().publicUrl("adoptions/1/a.jpg"))
                .isEqualTo("http://localhost:8081/local-images/adoptions/1/a.jpg");
    }
}
