package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "multi_poll")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPoll extends PanacheEntityBase {
    @Id
    public String id;
    public String anime;
    public String question;

    /** null = admin/system-owned; otherwise the AppUser.id who created it */
    public String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    public ContentStatus status = ContentStatus.APPROVED;

    @Column(name = "is_private", nullable = false)
    @Builder.Default
    public boolean isPrivate = false;

    @Column(name = "delete_pending", nullable = false)
    @Builder.Default
    public boolean deletePending = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    public Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("groupOrder ASC")
    public List<MultiPollGroup> groups;
}
