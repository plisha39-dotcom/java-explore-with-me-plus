package ewm.controller.admin;

import ewm.dto.NewUserRequest;
import ewm.dto.UserDto;
import ewm.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/admin/users")
public class UserAdminController {
    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> find(@RequestParam(required = false) List<Long> ids,
                              @RequestParam(defaultValue = "0") @Min(0) int from,
                              @RequestParam(defaultValue = "10") @Positive int size) {
        return userService.find(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest request) {
        return userService.create(request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}