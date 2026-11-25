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

import fr.insalyon.creatis.moteurlite.MoteurLiteException;

public class DownloadUtils {

    //TODO expose as setting ?
    public static int MAX_CONTENT_DISPOSITION_LENGTH = 8 * 1024;

    public static Optional<String> resolveRemoteFilename(URI downloadUrl) throws MoteurLiteException {
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
            Optional<String> contentDisposition = response.headers()
                    .firstValue("Content-Disposition")
                    .filter(value -> value.length() <= MAX_CONTENT_DISPOSITION_LENGTH);
            return contentDisposition.map(value -> getFilename(value));
        } catch (java.io.IOException | InterruptedException e) {
            throw new MoteurLiteException("Http error while resolving filename for URI " + downloadUrl.toString());
        }
    }

    //TODO if use spring at terms, better use Spring ContentDisposition
    public static final Pattern FILENAME_STAR = Pattern.compile(
            "(?:^|;)\\s*filename\\*\\s*=\\s*UTF-8''([^;\\s]+)", Pattern.CASE_INSENSITIVE);

    public static final Pattern FILENAME = Pattern.compile(
            "(?:^|;)\\s*filename\\s*=\\s*(?:\"((?:\\\\.|[^\"])*)\"|([^;\\s]*))", Pattern.CASE_INSENSITIVE);

    public static String getFilename(String header) {
        if (header == null)
            return null;

        Matcher extended = FILENAME_STAR.matcher(header);
        if (extended.find()) {
            try {
                // Preserve literal '+'; URLDecoder otherwise treats it as a space
                return URLDecoder.decode(
                        extended.group(1).replace("+", "%2B"),
                        StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ignored) {
                // Fallback to filename
            }
        }

        Matcher ordinary = FILENAME.matcher(header);
        if (!ordinary.find())
            return null;

        String filename = ordinary.group(1) != null
                ? ordinary.group(1).replace("\\\"", "\"").replace("\\\\", "\\")
                : ordinary.group(2);

        return filename.isEmpty() ? null : filename;
    }

}
