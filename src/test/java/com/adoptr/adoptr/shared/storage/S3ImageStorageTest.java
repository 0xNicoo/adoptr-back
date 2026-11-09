package com.adoptr.adoptr.shared.storage;

import com.adoptr.adoptr.shared.exception.custom.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class S3ImageStorageTest {

    private static final ProcessedImage IMAGE = new ProcessedImage(new byte[]{1, 2, 3}, "image/jpeg", "jpg");

    private final S3Client s3Client = mock(S3Client.class);

    private S3ImageStorage storage(String bucket, String publicBaseUrl) {
        return new S3ImageStorage(s3Client, bucket, "sa-east-1", publicBaseUrl);
    }

    @Test
    void subeAlBucketConTipoYCache() {
        storage("imagenes", "").upload("adoptions/1/a.jpg", IMAGE);

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().bucket()).isEqualTo("imagenes");
        assertThat(request.getValue().key()).isEqualTo("adoptions/1/a.jpg");
        assertThat(request.getValue().contentType()).isEqualTo("image/jpeg");
        assertThat(request.getValue().cacheControl()).contains("immutable");
    }

    @Test
    void siS3FallaResponde503() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("sin credenciales"));

        assertThatThrownBy(() -> storage("imagenes", "").upload("k", IMAGE))
                .isInstanceOfSatisfying(ServiceUnavailableException.class,
                        e -> assertThat(e.getCode()).isEqualTo("IMAGE_STORAGE_UNAVAILABLE"));
    }

    @Test
    void sinBucketConfiguradoResponde503SinLlamarAS3() {
        assertThatThrownBy(() -> storage("", "").upload("k", IMAGE))
                .isInstanceOf(ServiceUnavailableException.class);
        verifyNoInteractions(s3Client);
    }

    @Test
    void unBorradoFallidoNoInterrumpeLaOperacion() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(SdkClientException.create("caído"));

        assertThatCode(() -> storage("imagenes", "").delete("k")).doesNotThrowAnyException();
    }

    @Test
    void sinUrlBaseUsaLaDelBucket() {
        assertThat(storage("imagenes", "").publicUrl("adoptions/1/a.jpg"))
                .isEqualTo("https://imagenes.s3.sa-east-1.amazonaws.com/adoptions/1/a.jpg");
    }

    @Test
    void conUrlBaseLaUsaSinDuplicarLaBarra() {
        assertThat(storage("imagenes", "https://cdn.adoptr.test/").publicUrl("adoptions/1/a.jpg"))
                .isEqualTo("https://cdn.adoptr.test/adoptions/1/a.jpg");
    }
}
