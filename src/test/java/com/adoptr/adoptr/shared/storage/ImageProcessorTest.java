package com.adoptr.adoptr.shared.storage;

import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.PayloadTooLargeException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageProcessorTest {

    /** WebP de 1x1 píxel. */
    private static final String WEBP_1X1 = "UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEADsD+JaQAA3AAAAAA";
    private static final String GPS_MARKER = "GPS-LAT-34.92";

    private final ImageProcessor processor = new ImageProcessor();

    private static MockMultipartFile file(byte[] bytes, String declaredType) {
        return new MockMultipartFile("file", "foto", declaredType, bytes);
    }

    private static byte[] encode(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThat(ImageIO.write(image, format, out)).isTrue();
        return out.toByteArray();
    }

    private static BufferedImage rgb(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    private static BufferedImage decode(ProcessedImage image) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(image.bytes()));
    }

    /** JPEG con un bloque EXIF que trae orientación 6 (girar 90°) y un dato de ubicación de ejemplo. */
    private static byte[] jpegWithExif(BufferedImage image) throws IOException {
        byte[] jpeg = encode(image, "jpeg");
        byte[] marker = GPS_MARKER.getBytes(StandardCharsets.US_ASCII);
        ByteBuffer tiff = ByteBuffer.allocate(26 + marker.length);
        tiff.put(new byte[]{'M', 'M', 0, 42}).putInt(8);
        tiff.putShort((short) 1);
        tiff.putShort((short) 0x0112).putShort((short) 3).putInt(1).putShort((short) 6).putShort((short) 0);
        tiff.putInt(0);
        tiff.put(marker);
        byte[] exifHeader = "Exif\0\0".getBytes(StandardCharsets.US_ASCII);
        int segmentLength = 2 + exifHeader.length + tiff.capacity();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(jpeg, 0, 2);
        out.write(new byte[]{(byte) 0xFF, (byte) 0xE1, (byte) (segmentLength >> 8), (byte) segmentLength});
        out.write(exifHeader);
        out.write(tiff.array());
        out.write(jpeg, 2, jpeg.length - 2);
        return out.toByteArray();
    }

    @Test
    void reencodaAJpeg() throws IOException {
        ProcessedImage result = processor.process(file(encode(rgb(100, 50), "png"), "image/png"));

        assertThat(result.contentType()).isEqualTo("image/jpeg");
        assertThat(result.extension()).isEqualTo("jpg");
        BufferedImage decoded = decode(result);
        assertThat(decoded.getWidth()).isEqualTo(100);
        assertThat(decoded.getHeight()).isEqualTo(50);
    }

    @Test
    void aceptaWebp() throws IOException {
        ProcessedImage result = processor.process(file(Base64.getDecoder().decode(WEBP_1X1), "image/webp"));

        assertThat(decode(result).getWidth()).isEqualTo(1);
    }

    @Test
    void descartaLosMetadatosYAplicaLaOrientacion() throws IOException {
        byte[] original = jpegWithExif(rgb(100, 50));
        assertThat(new String(original, StandardCharsets.ISO_8859_1)).contains(GPS_MARKER);

        ProcessedImage result = processor.process(file(original, "image/jpeg"));

        String output = new String(result.bytes(), StandardCharsets.ISO_8859_1);
        assertThat(output).doesNotContain(GPS_MARKER).doesNotContain("Exif");
        BufferedImage decoded = decode(result);
        assertThat(decoded.getWidth()).isEqualTo(50);
        assertThat(decoded.getHeight()).isEqualTo(100);
    }

    @Test
    void achicaLasImagenesGrandesManteniendoLaProporcion() throws IOException {
        BufferedImage decoded = decode(processor.process(file(encode(rgb(3200, 1600), "jpeg"), "image/jpeg")));

        assertThat(decoded.getWidth()).isEqualTo(ImageProcessor.MAX_SIDE);
        assertThat(decoded.getHeight()).isEqualTo(ImageProcessor.MAX_SIDE / 2);
    }

    @Test
    void noAgrandaLasImagenesChicas() throws IOException {
        assertThat(decode(processor.process(file(encode(rgb(200, 100), "jpeg"), "image/jpeg"))).getWidth())
                .isEqualTo(200);
    }

    @Test
    void loTransparenteQuedaBlanco() throws IOException {
        BufferedImage transparent = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = transparent.createGraphics();
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(0, 0, 10, 10);
        graphics.dispose();

        BufferedImage decoded = decode(processor.process(file(encode(transparent, "png"), "image/png")));

        assertThat(new Color(decoded.getRGB(5, 5)).getRed()).isGreaterThan(250);
    }

    @Test
    void decideElFormatoPorElContenidoYNoPorLoDeclarado() {
        byte[] notAnImage = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> processor.process(file(notAnImage, "image/jpeg")))
                .isInstanceOfSatisfying(BadRequestException.class,
                        e -> assertThat(e.getCode()).isEqualTo("IMAGE_INVALID"));
    }

    @Test
    void rechazaFormatosFueraDeLaLista() throws IOException {
        byte[] gif = encode(rgb(10, 10), "gif");

        assertThatThrownBy(() -> processor.process(file(gif, "image/jpeg")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rechazaUnaImagenCorrupta() throws IOException {
        byte[] jpeg = encode(rgb(100, 100), "jpeg");
        byte[] truncated = new byte[30];
        System.arraycopy(jpeg, 0, truncated, 0, truncated.length);

        assertThatThrownBy(() -> processor.process(file(truncated, "image/jpeg")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rechazaUnArchivoVacio() {
        assertThatThrownBy(() -> processor.process(file(new byte[0], "image/jpeg")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rechazaUnArchivoDeMasDe5Mb() {
        byte[] tooBig = new byte[(int) ImageProcessor.MAX_FILE_BYTES + 1];

        assertThatThrownBy(() -> processor.process(file(tooBig, "image/jpeg")))
                .isInstanceOfSatisfying(PayloadTooLargeException.class,
                        e -> assertThat(e.getCode()).isEqualTo("IMAGE_TOO_LARGE"));
    }

    /** PNG liviano que declara más píxeles que el máximo: se rechaza sin decodificarlo. */
    @Test
    void rechazaDemasiadosPixelesSinCargarLaImagen() throws IOException {
        byte[] png = encode(rgb(1, 1), "png");
        ByteBuffer header = ByteBuffer.wrap(png);
        header.putInt(16, 50_000);
        header.putInt(20, 50_000);

        assertThatThrownBy(() -> processor.process(file(png, "image/png")))
                .isInstanceOf(PayloadTooLargeException.class);
    }

    @Test
    void conProporcionRecortaLosCostadosDeUnaFotoApaisada() throws IOException {
        BufferedImage image = rgb(400, 300);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 87, 300);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(87, 0, 226, 300);
        graphics.setColor(Color.RED);
        graphics.fillRect(313, 0, 87, 300);
        graphics.dispose();

        BufferedImage decoded = decode(processor.process(file(encode(image, "png"), "image/png"), 3, 4));

        assertThat(decoded.getWidth()).isEqualTo(225);
        assertThat(decoded.getHeight()).isEqualTo(300);
        Color left = new Color(decoded.getRGB(2, 150));
        assertThat(left.getBlue()).isGreaterThan(200);
        assertThat(left.getRed()).isLessThan(60);
    }

    @Test
    void conProporcionRecortaArribaYAbajoUnaFotoMuyAlta() throws IOException {
        BufferedImage decoded = decode(processor.process(file(encode(rgb(300, 600), "png"), "image/png"), 3, 4));

        assertThat(decoded.getWidth()).isEqualTo(300);
        assertThat(decoded.getHeight()).isEqualTo(400);
    }

    @Test
    void conProporcionAchicaYDespuesRecorta() throws IOException {
        BufferedImage decoded = decode(processor.process(file(encode(rgb(2000, 2000), "png"), "image/png"), 3, 4));

        assertThat(decoded.getWidth()).isEqualTo(1200);
        assertThat(decoded.getHeight()).isEqualTo(1600);
    }

    @Test
    void conProporcionRecortaDespuesDeGirarSegunExif() throws IOException {
        BufferedImage decoded = decode(processor.process(file(jpegWithExif(rgb(600, 300)), "image/jpeg"), 3, 4));

        assertThat(decoded.getWidth()).isEqualTo(300);
        assertThat(decoded.getHeight()).isEqualTo(400);
    }

    @Test
    void unaFotoYaEnProporcionNoSeRecorta() {
        BufferedImage exact = rgb(1200, 1600);
        BufferedImage offByOne = rgb(1200, 1601);

        assertThat(ImageProcessor.cropToRatio(exact, 3, 4)).isSameAs(exact);
        assertThat(ImageProcessor.cropToRatio(offByOne, 3, 4)).isSameAs(offByOne);
    }

    @Test
    void sinProporcionNoRecorta() throws IOException {
        BufferedImage decoded = decode(processor.process(file(encode(rgb(200, 100), "png"), "image/png")));

        assertThat(decoded.getWidth()).isEqualTo(200);
        assertThat(decoded.getHeight()).isEqualTo(100);
    }
}
