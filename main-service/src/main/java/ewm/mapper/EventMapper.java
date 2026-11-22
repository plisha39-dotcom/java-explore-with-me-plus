package ewm.mapper;

import ewm.dto.CategoryDto;
import ewm.dto.EventFullDto;
import ewm.dto.LocationDto;
import ewm.dto.NewEventDto;
import ewm.dto.UserShortDto;
import ewm.model.Category;
import ewm.model.Event;
import ewm.model.Location;
import ewm.model.User;

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

    public static EventFullDto toFullDto(Event event, Long views, Long confirmedRequests) {
        return EventFullDto.builder()
                           .id(event.getId())
                           .annotation(event.getAnnotation())
                           .category(toCategoryDto(event.getCategory()))
                           .confirmedRequests(confirmedRequests)
                           .createdOn(event.getCreatedOn())
                           .description(event.getDescription())
                           .initiator(toUserShortDto(event.getInitiator()))
                           .location(toLocationDto(event.getLocation()))
                           .paid(event.isPaid())
                           .participantLimit(event.getParticipantLimit())
                           .publishedOn(event.getPublishedOn())
                           .requestModeration(event.isRequestModeration())
                           .state(event.getState())
                           .title(event.getTitle())
                           .eventDate(event.getEventDate())
                           .views(views)
                           .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }

    public static UserShortDto toUserShortDto(User user) {
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(user.getId());
        userShortDto.setName(user.getName());
        return userShortDto;
    }

    public static LocationDto toLocationDto(Location location) {
        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());
        return locationDto;
    }
}
