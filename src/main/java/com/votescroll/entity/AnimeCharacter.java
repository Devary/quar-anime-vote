package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "character")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnimeCharacter extends PanacheEntityBase {
    @Id
    public String id;
    public String name;
    public String title;
    public String anime;
    @Column(length = 2048)
    public String imageUrl;

    /** null = admin/system-owned */
    public String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    public ContentStatus status = ContentStatus.APPROVED;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    public Instant createdAt = Instant.now();
}
