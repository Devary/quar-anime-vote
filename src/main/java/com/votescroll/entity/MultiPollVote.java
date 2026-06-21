package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "multi_poll_vote")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPollVote extends PanacheEntity {
    @Column(name = "poll_id", nullable = false)
    public String pollId;
    @Column(name = "character_id", nullable = false)
    public String characterId;
    @Column(name = "session_id")
    public String sessionId;
    @Column(name = "user_id")
    public String userId;
    @Column(name = "ip_address")
    public String ipAddress;
    public LocalDateTime votedAt;
}
