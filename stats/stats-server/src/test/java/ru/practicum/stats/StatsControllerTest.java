package ru.practicum.stats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import ru.practicum.stats.controller.StatsController;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.InvalidTimeRangeException;
import ru.practicum.stats.service.HitService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HitService hitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateHit() throws Exception {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp("ewm-main-service");
        endpointHit.setUri("/events/1");
        endpointHit.setIp("192.168.1.100");
        endpointHit.setTimestamp(LocalDateTime.of(2026, 11, 10, 14, 30, 0));
        String json = objectMapper.writeValueAsString(endpointHit);

        mockMvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON).content(json))
               .andExpect(status().isCreated());

        verify(hitService).save(any(EndpointHit.class));
    }

    @Test
    void shouldGetStatistics() throws Exception {
        ViewStats viewStats = new ViewStats();
        viewStats.setApp("ewm-main-service");
        viewStats.setUri("/events/1");
        viewStats.setHits(15L);

        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 20, 0);
        List<ViewStats> expected = List.of(viewStats);

        when(hitService.getStatistics(start, end, null, false))
                .thenReturn(expected);

        mockMvc.perform(get("/stats")
                       .param("start", "2026-11-10 10:00:00")
                       .param("end", "2026-11-10 20:00:00")
                       .param("unique", "false"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
               .andExpect(jsonPath("$[0].uri").value("/events/1"))
               .andExpect(jsonPath("$[0].hits").value(15))
               .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnBadRequestForInvalidTimeRange() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 21, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 20, 0);

        when(hitService.getStatistics(start, end, null, false))
                .thenThrow(new InvalidTimeRangeException("Некорректный интервал времени"));

        mockMvc.perform(get("/stats")
                       .param("start", "2026-11-10 21:00:00")
                       .param("end", "2026-11-10 20:00:00")
                       .param("unique", "false"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("Некорректный интервал времени"));
    }
}
