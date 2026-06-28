package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poll")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Poll extends PanacheEntityBase {
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

    // Legacy FK columns — kept non-null for existing data; always set to fighters[0/1]
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fighter1_id")
    public AnimeCharacter fighter1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fighter2_id")
    public AnimeCharacter fighter2;

    // Ordered fighters list (2-10 per poll); populated for all new/updated polls
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "poll_fighters",
        joinColumns = @JoinColumn(name = "poll_id"),
        inverseJoinColumns = @JoinColumn(name = "character_id")
    )
    @OrderColumn(name = "fighter_order")
    @Builder.Default
    public List<AnimeCharacter> fighters = new ArrayList<>();

    /** Returns fighters list when populated, otherwise falls back to fighter1/fighter2 for legacy rows. */
    public List<AnimeCharacter> effectiveFighters() {
        if (!fighters.isEmpty()) return fighters;
        List<AnimeCharacter> list = new ArrayList<>();
        if (fighter1 != null) list.add(fighter1);
        if (fighter2 != null) list.add(fighter2);
        return list;
    }
}
