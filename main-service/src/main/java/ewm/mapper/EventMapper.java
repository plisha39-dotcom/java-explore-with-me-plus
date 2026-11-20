package ewm.mapper;

import ewm.dto.NewEventDto;
import ewm.model.Category;
import ewm.model.Event;
import ewm.model.Location;
import ewm.model.User;

import java.time.LocalDateTime;

public class EventMapper {
    public static Event toEntity(NewEventDto eventDto, User initiator, Category category) {
        Location location = new Location();
        location.setLat(eventDto.getLocation().getLat());
        location.setLon(eventDto.getLocation().getLon());
        return Event.builder()
                    .category(category)
                    .initiator(initiator)
                    .annotation(eventDto.getAnnotation())
                    .description(eventDto.getDescription())
                    .eventDate(eventDto.getEventDate())
                    .paid(eventDto.isPaid())
                    .location(location)
                    .title(eventDto.getTitle())
                    .participantLimit(eventDto.getParticipantLimit())
                    .requestModeration(eventDto.isRequestModeration())
                    .build();
    }
}
