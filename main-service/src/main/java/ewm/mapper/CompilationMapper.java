package ewm.mapper;

import ewm.dto.CategoryDto;
import ewm.dto.CompilationDto;
import ewm.dto.EventShortDto;
import ewm.model.Compilation;
import ewm.model.Event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompilationMapper {
    public static CompilationDto toDto(Compilation compilation, Map<Long, Long> views,
                                       Map<Long, Long> confirmedRequests) {
        Set<EventShortDto> eventDtos = new HashSet<>();
        if (compilation.getEvents() != null) {
            for (Event event : compilation.getEvents()) {
                eventDtos.add(toEventShortDto(event, views, confirmedRequests));
            }
        }
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(eventDtos)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, Map<Long, Long> views,
                                                Map<Long, Long> confirmedRequests) {
        CategoryDto category = new CategoryDto(event.getCategory().getId(), event.getCategory().getName());
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(category)
                .confirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L))
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(views.getOrDefault(event.getId(), 0L))
                .build();
    }
}
