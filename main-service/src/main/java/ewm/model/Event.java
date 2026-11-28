package ewm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "category_id",
            nullable = false)
    private Category category;
    @ManyToOne
    @JoinColumn(name = "initiator_id",
            nullable = false)
    private User initiator;
    @Column(nullable = false,
            length = 2000)
    private String annotation;
    @Column(name = "created_on",
    nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    @Column(nullable = false,
            length = 7000)
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "event_date",
            nullable = false)
    private LocalDateTime eventDate;
    @Builder.Default
    private boolean paid = false;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(nullable = false,
            length = 120)
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    @Builder.Default
    private EventState state = EventState.PENDING;
    @Embedded
    private Location location;
    @Column(name = "participant_limit")
    @Builder.Default
    private int participantLimit = 0;
    @Column(name = "request_moderation")
    @Builder.Default
    private boolean requestModeration = true;
}
