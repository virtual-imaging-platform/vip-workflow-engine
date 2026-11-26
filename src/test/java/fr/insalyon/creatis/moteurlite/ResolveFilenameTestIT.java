package fr.insalyon.creatis.moteurlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;

import fr.insalyon.creatis.moteurlite.util.DownloadUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResolveFilenameTestIT {
    private HttpServer server;
    private HttpClient client;
    private AtomicReference<String> receivedMethod;
    private final int maxContentDispositionSize = 8192;

    @BeforeEach
    void startServer() throws IOException {
        receivedMethod = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/download", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename*=UTF-8''caf%C3%A9.pdf");
            // -1 = no body
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/directory/download", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"R07223.zip\"");
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/no-header", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        server.createContext("/long-header", exchange -> {
            String longValue = "attachment; filename=\"" +
                    "x".repeat(maxContentDispositionSize + 1) +
                    "\"";
            exchange.getResponseHeaders().set("Content-Disposition", longValue);
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        server.start();
    }

    @AfterEach
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
    }

    @Test
    public void headRequestParsesFilenameStarFromResponse() throws Exception {
        assertEquals("café.pdf", DownloadUtils.resolveRemoteFilename(uri("/download"), maxContentDispositionSize).orElseThrow());
    }

    @Test
    public void headRequestParsesFilenameFromResponse() throws Exception {
        assertEquals("R07223.zip", DownloadUtils.resolveRemoteFilename(uri("/directory/download"), maxContentDispositionSize).orElseThrow());
    }

    @Test
    public void returnsEmptyWhenHeaderIsMissing() throws Exception {
        assertTrue(DownloadUtils.resolveRemoteFilename(uri("/no-header"), maxContentDispositionSize).isEmpty());
    }

    @Test
    public void returnsEmptyWhenHeaderExceedsLimit() throws Exception {
        assertTrue(DownloadUtils.resolveRemoteFilename(uri("/long-header"), maxContentDispositionSize).isEmpty());
    }
}
