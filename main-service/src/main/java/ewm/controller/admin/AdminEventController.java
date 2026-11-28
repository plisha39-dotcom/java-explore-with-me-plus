package ewm.controller.admin;

import ewm.dto.EventFullDto;
import ewm.dto.UpdateEventAdminRequest;
import ewm.model.EventState;
import ewm.service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminEventController {
    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto update(@PathVariable Long eventId,
                               @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        return eventService.adminUpdate(eventId, updateEventAdminRequest);
    }

    @GetMapping("/events")
    public  List<EventFullDto> findAll(@RequestParam(required = false) List<Long> users,
                                       @RequestParam(required = false) List<EventState> states,
                                       @RequestParam(required = false) List<Long> categories,
                                       @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                       @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        return eventService.adminFindAll(users, states, categories, start, end, from, size);
    }
}
