package ru.mafteroid.probe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mafteroid.events.CheckCompletedEvent;
import ru.mafteroid.events.CheckRequestedEvent;
import ru.mafteroid.events.CheckType;
import ru.mafteroid.probe.client.HttpProbeClient;
import ru.mafteroid.probe.messaging.CheckCompletedPublisher;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProbeServiceTest {

    @Mock HttpClient http;
    @Mock HttpResponse<InputStream> response;
    @Mock CheckCompletedPublisher publisher;

    @ParameterizedTest
    @ValueSource(ints = {200, 500})
    void publishesHttpStatus(int status) throws Exception {
        when(http.<InputStream>send(any(), any())).thenReturn(response);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(InputStream.nullInputStream());

        var result = executeCheck();

        assertEquals(Integer.valueOf(status), result.httpStatus());
        assertEquals(status == 200, result.successful());
        assertEquals(status == 200 ? null : "HTTP_STATUS", result.errorCode());
    }

    @Test
    void publishesTimeoutWithoutHttpStatus() throws Exception {
        when(http.<InputStream>send(any(), any())).thenThrow(new HttpTimeoutException("Timeout"));

        var result = executeCheck();

        assertFalse(result.successful());
        assertNull(result.httpStatus());
        assertEquals("TIMEOUT", result.errorCode());
    }

    private CheckCompletedEvent executeCheck() throws InterruptedException {
        var request = new CheckRequestedEvent(UUID.randomUUID(), UUID.randomUUID(),
                "https://example.com", 1000, Instant.now(), CheckType.HTTP);
        var service = new ProbeService(List.of(new HttpProbeClient(http)), publisher);
        service.execute(request);

        var sent = ArgumentCaptor.forClass(CheckCompletedEvent.class);
        verify(publisher).publish(sent.capture());
        var result = sent.getValue();
        assertEquals(request.checkId(), result.checkId());
        assertEquals(request.monitorId(), result.monitorId());
        assertEquals(request.checkType(), result.checkType());
        return result;
    }
}
