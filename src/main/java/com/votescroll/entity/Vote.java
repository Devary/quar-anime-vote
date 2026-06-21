package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vote",
    uniqueConstraints = @UniqueConstraint(columnNames = {"poll_id", "session_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vote extends PanacheEntity {
    @Column(name = "poll_id", nullable = false)
    public String pollId;
    @Column(name = "character_id", nullable = false)
    public String characterId;
    @Column(name = "session_id")
    public String sessionId;
    public LocalDateTime votedAt;
}
