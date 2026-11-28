package ewm.service;

import ewm.dto.EventFullDto;
import ewm.dto.EventShortDto;
import ewm.model.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto getEventById(Long eventId, String remoteAddr, String requestURI);

    List<EventShortDto> getEvents(String text,
                                  Integer[] categories,
                                  boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  boolean onlyAvailable,
                                  Sort sort,
                                  Integer from,
                                  Integer size,
                                  String remoteAddr,
                                  String requestURI);
}
