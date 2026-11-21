package ewm.service;

import ewm.dto.NewUserRequest;
import ewm.dto.UserDto;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.mapper.UserMapper;
import ewm.model.User;
import ewm.repository.OffsetBasedPageRequest;
import ewm.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto create(NewUserRequest request) {
        String userEmail = request.getEmail();
        if (userRepository.existsByEmail(userEmail)) {
            throw new ConflictException("User with email=" + userEmail + " already exists");
        }
        return UserMapper.toDto(userRepository.save(UserMapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<UserDto> find(List<Long> ids, int from, int size) {
        List<User> users;
        if (ids != null) {
            users = userRepository.findAllById(ids).stream()
                    .sorted(Comparator.comparing(User::getId))
                    .toList();
        } else {
            users = userRepository.findAll(new OffsetBasedPageRequest(
                    from, size, Sort.by(Sort.Direction.ASC, "id"))).getContent();
        }
        return users.stream().map(UserMapper::toDto).toList();
    }

    public void delete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        userRepository.delete(user);
    }
}
