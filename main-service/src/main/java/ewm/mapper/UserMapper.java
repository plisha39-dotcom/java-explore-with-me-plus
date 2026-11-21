package ewm.mapper;

import ewm.dto.NewUserRequest;
import ewm.dto.UserDto;
import ewm.model.User;

public class UserMapper {
    public static User toEntity(NewUserRequest newUserRequest) {
        User user = new User();
        user.setEmail(newUserRequest.getEmail());
        user.setName(newUserRequest.getName());
        return user;
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }
}
