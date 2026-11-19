package ewm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Location {
    @Column(nullable = false)
    private Double lat;
    @Column(nullable = false)
    private Double lon;
}
