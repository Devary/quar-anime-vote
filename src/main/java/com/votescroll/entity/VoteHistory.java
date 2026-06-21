package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vote_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VoteHistory extends PanacheEntityBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "poll_id", nullable = false)
    public String pollId;

    @Column(name = "poll_type", nullable = false)
    public String pollType; // "single" or "multi"

    @Column(name = "char_id", nullable = false)
    public String charId;

    @Column(name = "user_id")
    public String userId;

    @Column(name = "ip_address")
    public String ipAddress;

    @Column(nullable = false)
    @Builder.Default
    public String action = "VOTE"; // "VOTE" or "CHANGE_VOTE"

    @Column(name = "voted_at")
    @Builder.Default
    public LocalDateTime votedAt = LocalDateTime.now();
}
