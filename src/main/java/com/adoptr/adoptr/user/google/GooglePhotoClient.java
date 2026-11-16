package com.adoptr.adoptr.user.google;

import com.adoptr.adoptr.shared.exception.custom.ServiceUnavailableException;
import com.adoptr.adoptr.shared.storage.ImageProcessor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class GooglePhotoClient {

    static final int SIZE = 800;
    private static final String ALLOWED_HOST_SUFFIX = ".googleusercontent.com";
    private static final Pattern SIZE_SUFFIX = Pattern.compile("=s\\d+(-c)?$");

    private final HttpClient httpClient;
    private final Duration timeout;
    private final long maxBytes;

    public GooglePhotoClient() {
        this(Duration.ofSeconds(5), ImageProcessor.MAX_FILE_BYTES);
    }

    GooglePhotoClient(Duration timeout, long maxBytes) {
        this.timeout = timeout;
        this.maxBytes = maxBytes;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public static boolean isAllowed(String url) {
        if (url == null) {
            return false;
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme())
                    && host != null
                    && host.toLowerCase(Locale.ROOT).endsWith(ALLOWED_HOST_SUFFIX)
                    && uri.getUserInfo() == null
                    && uri.getPort() == -1;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    static String sized(String url) {
        return SIZE_SUFFIX.matcher(url).replaceFirst("=s" + SIZE + "-c");
    }

    public byte[] download(String url) {
        if (!isAllowed(url)) {
            throw unavailable();
        }
        return fetch(URI.create(sized(url)));
    }

    byte[] fetch(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(timeout).GET().build();
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw unavailable();
                }
                byte[] bytes = body.readNBytes((int) maxBytes + 1);
                if (bytes.length == 0 || bytes.length > maxBytes) {
                    throw unavailable();
                }
                return bytes;
            }
        } catch (IOException e) {
            throw unavailable();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable();
        }
    }

    private static ServiceUnavailableException unavailable() {
        return new ServiceUnavailableException(GooglePhotoError.UNAVAILABLE);
    }
}
