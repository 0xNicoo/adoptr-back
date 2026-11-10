package com.adoptr.adoptr.shared.storage;

import com.adoptr.adoptr.shared.exception.custom.BadRequestException;
import com.adoptr.adoptr.shared.exception.custom.PayloadTooLargeException;
import com.adoptr.adoptr.shared.exception.custom.ServiceException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Valida el archivo por su contenido (no por lo que declara el cliente) y lo reencoda a JPEG.
 * Reencodar descarta los metadatos, entre ellos la ubicación GPS que suelen traer las fotos de
 * celular; antes aplica la orientación EXIF para que la foto no quede girada.
 */
@Component
public class ImageProcessor {

    public static final long MAX_FILE_BYTES = 5 * 1024 * 1024;
    static final long MAX_PIXELS = 40_000_000;
    static final int MAX_SIDE = 1600;
    private static final float JPEG_QUALITY = 0.85f;
    private static final Set<String> ALLOWED_FORMATS = Set.of("jpeg", "png", "webp");

    /** Decodificar una foto grande ocupa cientos de MB: se limita cuántas se procesan a la vez. */
    private final Semaphore permits = new Semaphore(2);

    public ImageProcessor() {
        // Registra el lector de WebP también cuando la app corre desde el jar de Spring Boot.
        ImageIO.scanForPlugins();
        ImageIO.setUseCache(false);
    }

    public ProcessedImage process(MultipartFile file) {
        return process(file, 0, 0);
    }

    /**
     * Igual que {@link #process(MultipartFile)}, pero recortada al centro con la proporción
     * {@code ratioWidth:ratioHeight} (por ejemplo 4:3) si no la tiene. Con 0 no recorta.
     */
    public ProcessedImage process(MultipartFile file, int ratioWidth, int ratioHeight) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ImageError.INVALID);
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new PayloadTooLargeException(ImageError.TOO_LARGE);
        }
        permits.acquireUninterruptibly();
        try {
            byte[] original = file.getBytes();
            int longestSide = inspect(original);
            Thumbnails.Builder<?> builder = Thumbnails.of(new ByteArrayInputStream(original));
            BufferedImage image = (longestSide > MAX_SIDE ? builder.size(MAX_SIDE, MAX_SIDE) : builder.scale(1.0))
                    .asBufferedImage();
            if (ratioWidth > 0 && ratioHeight > 0) {
                image = cropToRatio(image, ratioWidth, ratioHeight);
            }
            return new ProcessedImage(encodeJpeg(onWhiteBackground(image)), "image/jpeg", "jpg");
        } catch (ServiceException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            // Un archivo corrupto puede hacer fallar al decodificador de varias formas.
            throw new BadRequestException(ImageError.INVALID);
        } finally {
            permits.release();
        }
    }

    /** Lee formato y dimensiones sin decodificar los píxeles: frena imágenes enormes antes de cargarlas. */
    private static int inspect(byte[] bytes) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new BadRequestException(ImageError.INVALID);
            }
            ImageReader reader = readers.next();
            try {
                if (!ALLOWED_FORMATS.contains(reader.getFormatName().toLowerCase(Locale.ROOT))) {
                    throw new BadRequestException(ImageError.INVALID);
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if ((long) width * height > MAX_PIXELS) {
                    throw new PayloadTooLargeException(ImageError.TOO_LARGE);
                }
                return Math.max(width, height);
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * Recorta al centro lo que sobra para llegar a la proporción pedida. Se aplica después de girar
     * según EXIF y de achicar. Una diferencia de un píxel (redondeo de quien ya recortó) se deja como está.
     */
    static BufferedImage cropToRatio(BufferedImage image, int ratioWidth, int ratioHeight) {
        int width = image.getWidth();
        int height = image.getHeight();
        long wide = (long) width * ratioHeight;
        long tall = (long) height * ratioWidth;
        int targetWidth = width;
        int targetHeight = height;
        if (wide > tall) {
            targetWidth = Math.max(1, (int) Math.round((double) height * ratioWidth / ratioHeight));
        } else if (tall > wide) {
            targetHeight = Math.max(1, (int) Math.round((double) width * ratioHeight / ratioWidth));
        }
        if (Math.abs(width - targetWidth) <= 1 && Math.abs(height - targetHeight) <= 1) {
            return image;
        }
        return image.getSubimage((width - targetWidth) / 2, (height - targetHeight) / 2, targetWidth, targetHeight);
    }

    /** JPEG no tiene transparencia: lo transparente queda blanco en lugar de negro. */
    private static BufferedImage onWhiteBackground(BufferedImage image) {
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, Color.WHITE, null);
        } finally {
            graphics.dispose();
        }
        return rgb;
    }

    private static byte[] encodeJpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(JPEG_QUALITY);
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }
}
