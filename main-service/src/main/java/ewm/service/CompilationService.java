package ewm.service;

import client.StatClient;
import ewm.dto.CompilationDto;
import ewm.dto.NewCompilationDto;
import ewm.dto.UpdateCompilationRequest;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.mapper.CompilationMapper;
import ewm.model.Compilation;
import ewm.model.Event;
import ewm.model.RequestStatus;
import ewm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompilationService {
    private static final Logger log = LoggerFactory.getLogger(CompilationService.class);
    private static final DateTimeFormatter STATS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STATS_START = "2000-01-01 00:00:00";

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatClient statClient;

    public CompilationService(CompilationRepository compilationRepository,
                              EventRepository eventRepository,
                              ParticipationRequestRepository requestRepository,
                              StatClient statClient) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
        this.statClient = statClient;
    }

    @Transactional(readOnly = true)
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(
                from, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Compilation> page = pinned == null ? compilationRepository.findAll(pageable)
                : compilationRepository.findAllByPinned(pinned, pageable);

        List<Compilation> compilations = fetchCompilationsWithEvents(page.getContent());
        Collection<Event> events = collectEvents(compilations);
        Map<Long, Long> views = loadViews(events);
        Map<Long, Long> confirmedRequests = loadConfirmedRequests(events);
        return compilations.stream()
                .map(compilation -> CompilationMapper.toDto(compilation, views, confirmedRequests))
                .toList();
    }

    @Transactional(readOnly = true)
    public CompilationDto findById(Long compilationId) {
        Compilation compilation = getCompilationWithEvents(compilationId);
        Collection<Event> events = compilation.getEvents();
        return CompilationMapper.toDto(compilation, loadViews(events), loadConfirmedRequests(events));
    }

    @Transactional
    public CompilationDto create(NewCompilationDto request) {
        if (compilationRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Compilation with title=" + request.getTitle() + " already exists");
        }
        Compilation compilation = new Compilation();
        compilation.setTitle(request.getTitle());
        compilation.setPinned(request.isPinned());
        compilation.setEvents(resolveEvents(request.getEvents()));
        Compilation saved = compilationRepository.save(compilation);
        Collection<Event> events = saved.getEvents();
        return CompilationMapper.toDto(saved, loadViews(events), loadConfirmedRequests(events));
    }

    @Transactional
    public CompilationDto update(Long compilationId, UpdateCompilationRequest request) {
        Compilation compilation = getCompilationWithEvents(compilationId);
        if (request.getTitle() != null) {
            if (compilationRepository.existsByTitleAndIdNot(request.getTitle(), compilationId)) {
                throw new ConflictException("Compilation with title=" + request.getTitle() + " already exists");
            }
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null) {
            compilation.setEvents(resolveEvents(request.getEvents()));
        }
        Compilation saved = compilationRepository.save(compilation);
        Collection<Event> events = saved.getEvents();
        return CompilationMapper.toDto(saved, loadViews(events), loadConfirmedRequests(events));
    }

    @Transactional
    public void delete(Long compilationId) {
        compilationRepository.delete(getCompilation(compilationId));
    }

    private Compilation getCompilation(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compilationId + " was not found"));
    }

    private Compilation getCompilationWithEvents(Long compilationId) {
        return compilationRepository.findWithEventsById(compilationId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compilationId + " was not found"));
    }

    private List<Compilation> fetchCompilationsWithEvents(List<Compilation> pageContent) {
        if (pageContent.isEmpty()) {
            return List.of();
        }
        List<Long> compilationIds = pageContent.stream().map(Compilation::getId).toList();
        Map<Long, Compilation> fetchedById = compilationRepository.findAllWithEventsByIdIn(compilationIds)
                .stream()
                .collect(Collectors.toMap(Compilation::getId, Function.identity()));
        return pageContent.stream()
                .map(compilation -> fetchedById.get(compilation.getId()))
                .toList();
    }

    private Set<Event> resolveEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Event> foundEvents = eventRepository.findAllWithCategoryAndInitiatorByIdIn(eventIds);
        Set<Long> foundIds = foundEvents.stream().map(Event::getId).collect(Collectors.toSet());
        Set<Long> missingIds = new HashSet<>(eventIds);
        missingIds.removeAll(foundIds);
        if (!missingIds.isEmpty()) {
            Long missingId = missingIds.iterator().next();
            throw new NotFoundException("Event with id=" + missingId + " was not found");
        }
        return new HashSet<>(foundEvents);
    }

    private Collection<Event> collectEvents(List<Compilation> compilations) {
        Set<Event> events = new HashSet<>();
        for (Compilation compilation : compilations) {
            if (compilation.getEvents() != null) {
                events.addAll(compilation.getEvents());
            }
        }
        return events;
    }

    private Map<Long, Long> loadViews(Collection<Event> events) {
        Map<Long, Long> views = new HashMap<>();
        if (events == null || events.isEmpty()) {
            return views;
        }
        List<String> uris = events.stream().map(event -> "/events/" + event.getId()).toList();
        try {
            List<ViewStats> statistics = statClient.get(STATS_START,
                    LocalDateTime.now().format(STATS_DATE_FORMAT), uris, false);
            if (statistics != null) {
                for (ViewStats statistic : statistics) {
                    if (statistic.getUri() != null && statistic.getHits() != null) {
                        String eventId = statistic.getUri().substring(statistic.getUri().lastIndexOf('/') + 1);
                        try {
                            views.put(Long.parseLong(eventId), statistic.getHits());
                        } catch (NumberFormatException exception) {
                            log.debug("Ignoring unexpected statistics URI: {}", statistic.getUri());
                        }
                    }
                }
            }
        } catch (RestClientException exception) {
            log.warn("Statistics service is unavailable; event views are returned as zero");
        }
        return views;
    }

    private Map<Long, Long> loadConfirmedRequests(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }
        Set<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        return requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(EventConfirmedRequestCount::getEventId,
                        EventConfirmedRequestCount::getConfirmedCount));
    }
}
