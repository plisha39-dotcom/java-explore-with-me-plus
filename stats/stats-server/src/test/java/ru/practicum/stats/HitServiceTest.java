package ru.practicum.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.stats.exception.InvalidTimeRangeException;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.HitRepository;
import ru.practicum.stats.service.HitService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HitServiceTest {

    @Mock
    private HitRepository hitRepository;

    @InjectMocks
    private HitService hitService;

    @Test
    void shouldSaveHit() {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp("ewm-main-service");
        endpointHit.setUri("/events/1");
        endpointHit.setIp("192.168.1.100");
        endpointHit.setTimestamp(LocalDateTime.of(2026, 11, 10, 14, 30, 0));
        hitService.save(endpointHit);

        verify(hitRepository, times(1)).save(any(Hit.class));
    }

    @Test
    void shouldThrowExceptionWhenStartAfterEnd() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 15, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 10, 0);

        assertThrows(
                InvalidTimeRangeException.class,
                () -> hitService.getStatistics(start, end, null, false)
        );

        verifyNoInteractions(hitRepository);
    }

    @Test
    void shouldGetStatisticsWithoutUriFilterWhenUrisIsNull() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 5, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 10, 0);

        hitService.getStatistics(start, end, null, false);

        verify(hitRepository).noFilterByUris(start, end, false);

        verify(hitRepository, never()).filterByUris(
                any(), any(), anyList(), anyBoolean()
        );
    }

    @Test
    void shouldGetStatisticsWithoutUriFilterWhenUrisIsEmpty() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 5, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 10, 0);

        hitService.getStatistics(start, end, List.of(), false);

        verify(hitRepository).noFilterByUris(start, end, false);

        verify(hitRepository, never()).filterByUris(
                any(), any(), anyList(), anyBoolean()
        );
    }

    @Test
    void shouldGetStatisticsWithUriFilterWhenUrisProvided() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 5, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 10, 0);
        List<String> uris = List.of("/events/1", "/events/2");

        hitService.getStatistics(start, end, uris, true);

        verify(hitRepository).filterByUris(start, end, uris, true);

        verifyNoMoreInteractions(hitRepository);
    }

    @Test
    void shouldReturnStatisticsFromRepository() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 5, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 10, 0);
        ViewStats viewStats = new ViewStats("ewm-main-service", "/events/1", 15L);
        List<ViewStats> expected = List.of(viewStats);

        when(hitRepository.noFilterByUris(start, end, false)).thenReturn(expected);

        List<ViewStats> actual = hitService.getStatistics(start, end, null, false);

        assertEquals(expected, actual);
    }
}
