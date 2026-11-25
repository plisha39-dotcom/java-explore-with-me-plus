package ewm.service;

import ewm.dto.EventFullDto;
import ewm.dto.EventShortDto;
import ewm.dto.NewEventDto;
import ewm.exception.BadRequestException;
import ewm.exception.NotFoundException;
import ewm.mapper.EventMapper;
import ewm.model.Category;
import ewm.model.Event;
import ewm.model.User;
import ewm.repository.CategoryRepository;
import ewm.repository.EventRepository;
import ewm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                                              .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Неверная дата события");
        }
        Event event = EventMapper.toEntity(newEventDto, user, category);
        event.setCreatedOn(LocalDateTime.now());
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullDto(savedEvent, 0L, 0L);
    }

    public List<EventShortDto> findAllByUser(Long userId, int from, int size) {
        userRepository.findById(userId)
                      .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiator_Id(userId, pageable);
        return events.stream()
                     .map(event -> EventMapper.toShortDto(event, 0L, 0L))
                     .toList();
    }

    @Transactional(readOnly = true)
    public EventFullDto findUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId)
                                     .orElseThrow(() -> new NotFoundException("Событие с ID: " + eventId + " не найдено"));
        return EventMapper.toFullDto(event, 0L, 0L);
    }
}