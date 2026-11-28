package ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ewm.model.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewEventDto {
    @NotBlank
    private String annotation;
    @NotNull
    private Long category;
    @NotBlank
    private String description;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull
    @Valid
    private Location location;
    private boolean paid = false;
    private int participantLimit = 0;
    private boolean requestModeration = true;
    @NotBlank
    private String title;
}
