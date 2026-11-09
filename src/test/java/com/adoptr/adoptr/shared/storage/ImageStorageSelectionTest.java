package com.adoptr.adoptr.shared.storage;

import com.adoptr.adoptr.shared.config.LocalImagesWebConfig;
import com.adoptr.adoptr.shared.config.S3Config;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

/** El valor por defecto importa: sin la variable, un servidor no debe guardar imágenes en su disco. */
class ImageStorageSelectionTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(S3Config.class, S3ImageStorage.class,
                    LocalImagesWebConfig.class, LocalImageStorage.class)
            .withPropertyValues(
                    "aws.region=sa-east-1",
                    "aws.s3.bucket=imagenes",
                    "aws.s3.public-base-url=",
                    "app.images.local.dir=local-images",
                    "app.images.local.base-url=http://localhost:8081/local-images");

    @Test
    void sinLaVariableUsaS3() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(ImageStorage.class);
            assertThat(context.getBean(ImageStorage.class)).isInstanceOf(S3ImageStorage.class);
            assertThat(context).doesNotHaveBean(LocalImagesWebConfig.class);
        });
    }

    @Test
    void conS3UsaS3() {
        runner.withPropertyValues("app.images.storage=s3").run(context -> {
            assertThat(context.getBean(ImageStorage.class)).isInstanceOf(S3ImageStorage.class);
            assertThat(context).doesNotHaveBean(LocalImagesWebConfig.class);
        });
    }

    @Test
    void conLocalUsaLaCarpetaYNoCreaElClienteDeS3() {
        runner.withPropertyValues("app.images.storage=local").run(context -> {
            assertThat(context).hasSingleBean(ImageStorage.class);
            assertThat(context.getBean(ImageStorage.class)).isInstanceOf(LocalImageStorage.class);
            assertThat(context).hasSingleBean(LocalImagesWebConfig.class);
            assertThat(context).doesNotHaveBean(S3Client.class);
        });
    }

    @Test
    void conUnValorDesconocidoNoHayAlmacenamiento() {
        runner.withPropertyValues("app.images.storage=otro").run(context ->
                assertThat(context).doesNotHaveBean(ImageStorage.class));
    }
}
