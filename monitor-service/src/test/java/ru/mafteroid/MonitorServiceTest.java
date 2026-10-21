package ru.mafteroid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mafteroid.entity.Monitor;
import ru.mafteroid.exception.InvalidMonitorConfigurationException;
import ru.mafteroid.repository.MonitorRepository;
import ru.mafteroid.web.dto.CreateMonitorRequest;
import ru.mafteroid.web.dto.MonitorResponse;
import ru.mafteroid.web.service.MonitorService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonitorServiceTest {
    @Mock
    private MonitorRepository monitorRepository;
    @InjectMocks
    private MonitorService monitorService;

    @Test
    public void shouldCreateAndReturnValidMonitor() {

        CreateMonitorRequest request = new CreateMonitorRequest(
                "name",
                "http://example.com",
                30,
                1000
        );

        Mockito.when(monitorRepository.save(Mockito.any(Monitor.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));


        MonitorResponse response = monitorService.createMonitor(request);

        assertEquals(request.name(), response.name());
        assertEquals(request.url(), response.url());
        assertEquals(request.intervalSeconds(), response.intervalSeconds());
        assertEquals(request.timeoutMs(), response.timeoutMs());

        assertTrue(response.enabled());
        assertNotNull(response.createdAt());
        verify(monitorRepository, times(1)).save(Mockito.any(Monitor.class));
    }

    @Test
    public void shouldRejectInvalidInterval(){

        CreateMonitorRequest request = new CreateMonitorRequest(
                "Main site",
                "https://example.com",
                45,
                5000
        );

        assertThrows(InvalidMonitorConfigurationException.class, () -> {
            monitorService.createMonitor(request);
        });
        verifyNoInteractions(monitorRepository);
    }

    @Test
    public void shouldValidateUrl(){
        String name = "name";
        Integer interval = 30;
        Integer timeout = 5000;

        CreateMonitorRequest differentProtocol = new CreateMonitorRequest(
                name,
                "ftp://example.com",
                interval,
                timeout
        );
        CreateMonitorRequest emptyHost = new CreateMonitorRequest(
                name,
                "https://",
                interval,
                timeout
        );
        CreateMonitorRequest containsCredentials = new CreateMonitorRequest(
                name,
                "https://user:password@example:8080/data",
                interval,
                timeout
        );
        CreateMonitorRequest invalidUrl = new CreateMonitorRequest(
                name,
                "THERE_IS_NO_URL",
                interval,
                timeout
        );


        assertThrows(InvalidMonitorConfigurationException.class, () -> {
            monitorService.createMonitor(differentProtocol);
        });
        assertThrows(InvalidMonitorConfigurationException.class, () -> {
            monitorService.createMonitor(emptyHost);
        });
        assertThrows(InvalidMonitorConfigurationException.class, () -> {
            monitorService.createMonitor(containsCredentials);
        });
        assertThrows(InvalidMonitorConfigurationException.class, () -> {
            monitorService.createMonitor(invalidUrl);
        });


        verifyNoInteractions(monitorRepository);
    }

    @Test
    public void timeoutShouldBeLessThanInterval(){
        CreateMonitorRequest equals = new CreateMonitorRequest(
                "name",
                "https://example.com",
                30,
                30 * 1000
        );

        CreateMonitorRequest more = new CreateMonitorRequest(
                "name",
                "https://example.com",
                30,
                50 * 1000
        );

        CreateMonitorRequest less = new CreateMonitorRequest(
                "name",
                "https://example.com",
                30,
                20 * 1000
        );


        Mockito.when(monitorRepository.save(Mockito.any(Monitor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        assertThrows(InvalidMonitorConfigurationException.class, () -> {
            monitorService.createMonitor(equals);
        });
        assertThrows(InvalidMonitorConfigurationException.class, () -> {
            monitorService.createMonitor(more);
        });

        assertDoesNotThrow(() -> {
            monitorService.createMonitor(less);
        });
    }


}
