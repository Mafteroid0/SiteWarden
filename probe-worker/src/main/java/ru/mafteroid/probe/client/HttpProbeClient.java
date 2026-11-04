package ru.mafteroid.probe.client;

import org.springframework.stereotype.Component;
import ru.mafteroid.events.CheckType;
import ru.mafteroid.probe.model.ProbeResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class HttpProbeClient implements ProbeClient {

    private final HttpClient httpClient;

    public HttpProbeClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean supports(CheckType checkType) {
        return checkType == CheckType.HTTP;
    }

    @Override
    public ProbeResult probe(URI target, Duration timeout)
            throws InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(target)
                .timeout(timeout)
                .GET()
                .build();

        long startedAt = System.nanoTime();

        try {
            HttpResponse<InputStream> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            long durationMs = elapsedMs(startedAt);

            try (InputStream body = response.body()) {
                int status = response.statusCode();
                boolean successful = status >= 200 && status < 300;

                return new ProbeResult(
                        successful,
                        status,
                        durationMs,
                        successful ? null : "HTTP_STATUS"
                );
            }
        } catch (HttpTimeoutException exception) {
            return new ProbeResult(
                    false,
                    null,
                    elapsedMs(startedAt),
                    "TIMEOUT"
            );
        } catch (IOException exception) {
            return new ProbeResult(
                    false,
                    null,
                    elapsedMs(startedAt),
                    "IO_ERROR"
            );
        }
    }

    private long elapsedMs(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - startedAt
        );
    }
}