package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "multi_poll_group")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPollGroup extends PanacheEntityBase {
    @Id
    public String id;
    public String label;
    public int groupOrder;
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
    public List<AnimeCharacter> candidates;
}
