package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "multi_poll_group")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPollGroup extends PanacheEntityBase {
    @Id
    public String id;
    public String label;
    public int groupOrder;

    @Column(name = "group_level", nullable = false)
    @Builder.Default
    public int level = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "multi_poll_group_feeders",
        joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "feeder_group_id")
    @OrderColumn(name = "feeder_order")
    @Builder.Default
    public List<String> feederGroupIds = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "poll_id")
    public MultiPoll poll;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "multi_poll_group_candidate",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "character_id")
    )
    @OrderColumn(name = "candidate_order")
    @Builder.Default
    public List<AnimeCharacter> candidates = new ArrayList<>();

    @Column(name = "start_date")
    public Instant startDate;
    @Column(name = "end_date")
    public Instant endDate;

    public boolean isResolved() {
        return level == 0 || !candidates.isEmpty();
    }
}
