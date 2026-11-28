package ewm.service;

import client.StatClient;
import ewm.dto.EventFullDto;
import ewm.dto.EventShortDto;
import ewm.mapper.EventMapper;
import ewm.model.Event;
import ewm.model.EventState;
import ewm.model.Sort;
import ewm.repository.EventRepository;
import ewm.repository.ParticipationRequestRepository;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHit;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class EventServiceImpl {
    private final EventRepository eventRepository;
    private final StatClient statClient;
    private final ParticipationRequestRepository participationRequestRepository;

    public EventServiceImpl(EventRepository eventRepository,
                            StatClient statClient,
                            ParticipationRequestRepository participationRequestRepository) {
        this.eventRepository = eventRepository;
        this.statClient = statClient;
        this.participationRequestRepository = participationRequestRepository;
    }

    public List<EventShortDto> getEvents(String text,
                                         Integer[] categories,
                                         boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         boolean onlyAvailable,
                                         Sort sort,
                                         Integer from,
                                         Integer size,
                                         String remoteAddr,
                                         String requestURI) {
        List<Event> events;

        EndpointHit endpointHit = new EndpointHit("ewm-main-service", requestURI, remoteAddr, LocalDateTime.now());
        statClient.hit(endpointHit);

        if (categories == null || categories.length == 0) {
            events = eventRepository.searchEventsWithoutCategories(
                    text,
                    paid,
                    rangeStart,
                    rangeEnd,
                    EventState.PUBLISHED
            );
        } else {
            events = eventRepository.searchEvents(
                    text,
                    Arrays.asList(categories),
                    paid,
                    rangeStart,
                    rangeEnd,
                    EventState.PUBLISHED
            );
        }

        Comparator<EventShortDto> comparator = switch (sort) {
            case EVENT_DATE -> Comparator.comparing(EventShortDto::getEventDate);

            case VIEWS -> Comparator.comparing(
                    EventShortDto::getViews,
                    Comparator.reverseOrder()
            );
        };
        if (!onlyAvailable) {
            return events.stream()
                    .map(event -> EventMapper.toShortDto(
                            event,
                            statClient.get(event.getPublishedOn().toString(),
                                            LocalDateTime.now().toString(),
                                            List.of(requestURI),
                                            true)
                                    .getFirst().getHits(),
                            Integer.toUnsignedLong(participationRequestRepository.findByEventId(event.getId()).size()))
                    )
                    .sorted(comparator)
                    .skip(from)
                    .limit(size)
                    .toList();
        } else {
            return events.stream()
                    .filter(e -> e.getParticipantLimit() >= Integer.toUnsignedLong(
                            participationRequestRepository.findByEventId(e.getId()).size()))
                    .map(event -> EventMapper.toShortDto(
                            event,
                            statClient.get(event.getPublishedOn().toString(),
                                            LocalDateTime.now().toString(),
                                            List.of(requestURI),
                                            true)
                                    .getFirst().getHits(),
                            Integer.toUnsignedLong(participationRequestRepository.findByEventId(event.getId()).size()))
                    )
                    .sorted(comparator)
                    .skip(from)
                    .limit(size)
                    .toList();
        }
    }

    public EventFullDto getEventById(Long eventId, String remoteAddr, String requestURI) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        EndpointHit endpointHit = new EndpointHit("ewm-main-service", requestURI, remoteAddr, LocalDateTime.now());
        statClient.hit(endpointHit);
        return EventMapper.toFullDto(
                event,
                statClient
                        .get(event.getPublishedOn().toString(), LocalDateTime.now().toString(),
                                List.of(requestURI), true).getFirst().getHits(),
                Integer.toUnsignedLong(participationRequestRepository.findByEventId(event.getId()).size())
        );
    }
}
