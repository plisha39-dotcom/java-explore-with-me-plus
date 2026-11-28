package ewm.controller;

import ewm.dto.EventFullDto;
import ewm.dto.EventShortDto;
import ewm.model.Sort;
import ewm.service.EventServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController("/events")
public class EventsController {
    public final EventServiceImpl eventService;

    public EventsController(EventServiceImpl eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam String text,
                                         @RequestParam Integer[] categories,
                                         @RequestParam boolean paid,
                                         @RequestParam LocalDateTime rangeStart,
                                         @RequestParam LocalDateTime rangeEnd,
                                         @RequestParam boolean onlyAvailable,
                                         @RequestParam Sort sort,
                                         @RequestParam Integer from,
                                         @RequestParam Integer size,
                                         HttpServletRequest request) {
        return eventService.getEvents(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                request.getRemoteAddr(),
                request.getRequestURI());
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable("id") long eventId, HttpServletRequest request) {
        return eventService.getEventById(eventId, request.getRemoteAddr(), request.getRequestURI());
    }
}
