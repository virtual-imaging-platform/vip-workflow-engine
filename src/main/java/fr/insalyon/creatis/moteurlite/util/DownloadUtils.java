package fr.insalyon.creatis.moteurlite.util;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.ContentDisposition;

import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.moteurlite.MoteurLiteConfiguration;

public class DownloadUtils {

    public static Optional<String> resolveRemoteFilename(URI downloadUrl, int maxContentDispositionLength)
            throws MoteurLiteException {
        URI uri = downloadUrl;

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            Optional<String> contentDispositionStr = response.headers()
                    .firstValue("Content-Disposition")
                    .filter(value -> value.length() <= maxContentDispositionLength);
            if (contentDispositionStr.isPresent()) {
                return Optional.ofNullable(getFilename(contentDispositionStr.get()));
            } else {
                return contentDispositionStr;
            }
        } catch (java.io.IOException | InterruptedException e) {
            throw new MoteurLiteException("Http error while resolving filename for URI " + downloadUrl.toString(), e);
        }
    }

    public static ContentDisposition parseWithFallback(String header) {
        try {
            return ContentDisposition.parse(header);
        } catch (IllegalArgumentException ex) {
            // filename* might be invalid; try removing it (everything until a semicolon)
            if (header.matches("(?is).*;\\s*filename\\*\\s*=.*")) {
                String withoutExtended = header.replaceAll("(?i);\\s*filename\\*\\s*=\\s*[^;]*", "");
                return ContentDisposition.parse(withoutExtended);
            }
            throw ex;
        }
    }

    public static String getFilename(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        return parseWithFallback(header).getFilename();
    }

}
