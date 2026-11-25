package ewm.controller.user;

import ewm.dto.EventFullDto;
import ewm.dto.EventShortDto;
import ewm.dto.NewEventDto;
import ewm.service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class EventPrivateController {
    private final EventService eventService;

    public EventPrivateController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> findAllByUser(@PathVariable Long userId,
                                             @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                             @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        return eventService.findAllByUser(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto findUserEvent(@PathVariable Long userId,
                                            @PathVariable Long eventId) {
        return eventService.findUserEvent(userId, eventId);
    }
}
