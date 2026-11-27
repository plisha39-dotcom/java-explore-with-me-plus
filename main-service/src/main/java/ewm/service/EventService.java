package ewm.service;

import ewm.dto.EventFullDto;
import ewm.dto.EventShortDto;
import ewm.dto.NewEventDto;
import ewm.dto.UpdateEventAdminRequest;
import ewm.dto.UpdateEventUserRequest;
import ewm.exception.BadRequestException;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.mapper.EventMapper;
import ewm.model.Category;
import ewm.model.Event;
import ewm.model.EventState;
import ewm.model.Location;
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

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
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

    @Transactional(readOnly = true)
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

    @Transactional
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId)
                                     .orElseThrow(() -> new NotFoundException("Событие с ID: " + eventId + " не найдено"));
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Изменить можно только со статусом PENDING или CANCELED");
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                                                  .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }
        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Неверная дата события");
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            event.setState(switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW -> EventState.PENDING;
                case CANCEL_REVIEW -> EventState.CANCELED;
            });
        }
        EventMapper.applyUserUpdate(event, updateEventUserRequest);
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullDto(savedEvent, 0L, 0L);

    }

    @Transactional
    public EventFullDto adminUpdate(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new NotFoundException("Событие с ID: " + eventId + " не найдено"));
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                                                  .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Неверная дата события");
            }
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }
        EventMapper.applyAdminUpdate(event, updateEventAdminRequest);
        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Нельзя изменить опубликованное событие");
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Дата начала события должна быть не ранее чем через час");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Нельзя изменить опубликованное событие");
                    }
                    event.setState(EventState.CANCELED);
                }
            }
        }
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullDto(savedEvent, 0L, 0L);
    }
}