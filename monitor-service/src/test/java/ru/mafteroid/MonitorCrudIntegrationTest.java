package ru.mafteroid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mafteroid.entity.Monitor;
import ru.mafteroid.repository.MonitorRepository;
import ru.mafteroid.web.dto.CreateMonitorRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class MonitorCrudIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        monitorRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveMonitor() throws Exception {
        CreateMonitorRequest request = new CreateMonitorRequest(
                "Google",
                "https://google.com",
                60,
                5000
        );

        MvcResult result = mockMvc.perform(
                        post("/monitors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Google"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andReturn();

        UUID id = extractId(result);

        Monitor saved = monitorRepository.findById(id).orElseThrow();

        assertEquals(request.name(), saved.getName());
        assertEquals(request.url(), saved.getUrl());
        assertEquals(request.intervalSeconds(), saved.getIntervalSeconds());
        assertEquals(request.timeoutMs(), saved.getTimeoutMs());
        assertTrue(saved.getEnabled());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getNextCheckAt());

        mockMvc.perform(get("/monitors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Google"))
                .andExpect(jsonPath("$.url").value("https://google.com"))
                .andExpect(jsonPath("$.intervalSeconds").value(60))
                .andExpect(jsonPath("$.timeoutMs").value(5000))
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(get("/monitors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Google"));
    }

    @Test
    void shouldReturn404WhenMonitorNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/monitors/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Monitor not found"));
    }

    @Test
    void shouldReturn400ForInvalidDto() throws Exception {
        CreateMonitorRequest request = new CreateMonitorRequest(
                "",
                "https://google.com",
                60,
                5000
        );

        mockMvc.perform(
                        post("/monitors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        assertEquals(0, monitorRepository.count());
    }

    @Test
    void shouldReturn400ForInvalidInterval() throws Exception {
        CreateMonitorRequest request = new CreateMonitorRequest(
                "Google",
                "https://google.com",
                45,
                5000
        );

        mockMvc.perform(
                        post("/monitors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid monitor configuration"));

        assertEquals(0, monitorRepository.count());
    }

    @Test
    void shouldPauseResumeAndDeleteMonitor() throws Exception {
        UUID id = createMonitor();

        mockMvc.perform(
                        patch("/monitors/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"enabled\":false}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        Monitor paused = monitorRepository.findById(id).orElseThrow();

        assertFalse(paused.getEnabled());
        assertNull(paused.getNextCheckAt());

        mockMvc.perform(get("/monitors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(
                        patch("/monitors/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"enabled\":true}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        Monitor resumed = monitorRepository.findById(id).orElseThrow();

        assertTrue(resumed.getEnabled());
        assertNotNull(resumed.getNextCheckAt());

        mockMvc.perform(delete("/monitors/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(monitorRepository.existsById(id));

        mockMvc.perform(get("/monitors/{id}", id))
                .andExpect(status().isNotFound());
    }

    private UUID createMonitor() throws Exception {
        CreateMonitorRequest request = new CreateMonitorRequest(
                "Google",
                "https://google.com",
                60,
                5000
        );

        MvcResult result = mockMvc.perform(
                        post("/monitors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        return extractId(result);
    }

    private UUID extractId(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();

        String id = objectMapper.readTree(responseBody)
                .get("id")
                .asText();

        return UUID.fromString(id);
    }
}